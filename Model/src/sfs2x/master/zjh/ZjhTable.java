package sfs2x.master.zjh;


import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import org.apache.commons.lang.math.RandomUtils;
import sfs2x.master.Ibase.ISeat;
import sfs2x.master.Ibase.ITable;
import sfs2x.master.Player;
import sfs2x.utils.DBUtil;

import java.util.ArrayList;
import java.util.Collections;

public class ZjhTable<T extends ZjhSeat> extends ITable<T> {
    public static final int MAX_TURN = 50;
    //牌型
    /**
     * 杂花235 特殊牌
     */
    public final int SPECIAL = 0;
    /**
     *  散牌
     */
    private final int HIGH_CARD = 1;//散牌
    /**
     * 对子
     */
    private final int PAIR = 2;//对子
    /**
     * 对子
     */
    private final int STRAIGHT;//顺子
    /**
     * 同花
     */
    private final int FLUSH;//同花
    /**
     * 同花顺
     */
    private final int STRAIGHT_FLUSH = 5;//同花顺
    /**
     * 豹子
     */
    public final int THREE_KIND = 6;//豹子
    /**
     * 必闷,0 为无必闷,值为1,2,3(轮)
     */
    public int men;
    /**
     * true 同花大, false 顺子大
     */
    private final boolean sf;
    /**
     * 庄家的位置
     */
    public T banker;
    /**
     * 所有扑克
     */
    public ArrayList<Integer> pokers;
    /**
     * 当前操作的位置
     */
    public T current;
    /**
     * 最后下注
     */
    public int lastCall;
    /**
     * 最后下注是否盲注
     */
    public boolean lastBlind;
    /**
     *  桌面总筹码
     */
    public ArrayList<Integer> tableChips;
    /**
     * 一回合游戏的轮数
     */
    public int turn;
    /**
     * 上把赢了的位置
     */
    public T winner;
    /**
     * 底分
     */
    public int floor;
    /**
     * 最后下注的玩家的位置
     */
    public T lastAction;
    /**
     *  扎金花房卡房,构造方法
     * @param mod 游戏类型
     * @param aa 是否AA制
     * @param person 游戏局数
     * @param men 必闷
     * @param sf 规则,true 金花大 false 顺子大
     * @param need 游戏消耗的房卡
     */
    @SuppressWarnings("unchecked")
    public ZjhTable(int mod, boolean aa, int owner, int person, int count, int men, boolean sf, int need){
        super(mod,aa,true,owner,person,count,need);
        this.men = men;
        this.sf = sf;
        seats = (T[]) new ZjhSeat[person];
        for (int i=0;i<person;i++)
            seats[i] = (T) new ZjhSeat(i);
        banker = null;
        pokers = new ArrayList<>();
        current = null;
        lastCall = 0;
        lastBlind = true;
        tableChips = new ArrayList<>();
        turn = 0;
        winner = null;
        floor = 1;
        if (sf) {
            FLUSH = 4;
            STRAIGHT = 3;
        }else {
            FLUSH = 3;
            STRAIGHT = 4;
        }
    }

    @Override
    public boolean readyToStart() {
        if (roundStart)
            return false;
        playSeat.clear();
        for (T s:seats){
            if (!s.empty && s.ready)
                playSeat.add(s);
        }
        return playSeat.size() >= 2 && playSeat.size() == curPerson();
    }

    public ISFSObject toSFSObject(Player p) {
        T cSeat =  getSeat(p);
        ISFSObject object =  super.toSFSObject(p);
        object.putInt("m",men);
        object.putBool("sf",sf);
        object.putIntArray("chip",tableChips);
        object.putInt("lc",lastCall);
        object.putBool("lb",lastBlind);
        object.putInt("turn",turn);
        ISFSArray array = new SFSArray();
        for (T s:seats){
            if (!s.empty){
                ISFSObject o = s.toSFSObject(cSeat);
                if (cardRoom)
                    o.putLong("s",s.score);
                else {
                    //金币房
                }
                array.addSFSObject(o);
            }
        }
        object.putSFSArray("u",array);
        return object;
    }

    @Override
    public void initStartGame() {
        for (ISeat s:playSeat){
            s.initStartGame();
        }
        lastCall = 0;
        lastBlind = true;
        tableChips.clear();
        turn = 1;
        currentCount++;
        T oSeat = getSeat(getPlayer(owner));
        if (winner != null && playSeat.contains(winner))
            banker = winner;
        else if ( oSeat != null && playSeat.contains(oSeat))
            banker = oSeat;
        else{
            int i = RandomUtils.nextInt(playSeat.size());
            banker = playSeat.get(i);
        }
    }
    /**
     * 获取下一个叫分的玩家
     */
    public T getNextActionSeat(T cur){
        int index = playSeat.indexOf(cur);
        T next = null;
        while (next == null || next.action == 4 || next.action == 5)
            next = index+1 == playSeat.size()?playSeat.get(0):playSeat.get(index+1);
        return next;
    }

    public int getAvailableCount(){
        int n = 0;
        for (T s:playSeat){
            if (s.action != 4 && s.action != 5)
                n++;
        }
        return n;
    }

    /**
     * 计算牌型
     *
     * @param seat 计算该位置的牌型
     */
    public void analyzeType(T seat) {
        if (seat.hand.size() != 3)
            return;
        ArrayList<Integer> hand = new ArrayList<>(seat.hand);
        Collections.sort(hand);
        int a = hand.get(0) / 4, b = hand.get(1) / 4, c = hand.get(2) / 4;
        int ac = hand.get(0) % 4, bc = hand.get(1) % 4, cc = hand.get(2) % 4;
        //豹子
        if (a == b && a == c) {
            seat.type = THREE_KIND;
            seat.arr = new int[]{a};
            return;
        }
        //同花顺
        if (ac == bc && ac == cc) {
            if (a + 1 == b && b + 1 == c) {
                seat.type = STRAIGHT_FLUSH;
                seat.arr = new int[]{c, b, a};
                return;
            } else if (a == 2 && b == 3 && c == 14) {
                seat.type = STRAIGHT_FLUSH;
                if (sf)
                    seat.arr = new int[]{3, 2, 1};
                else
                    seat.arr = new int[]{c, b, a};
                return;
            }
        }
        //同花
        if (ac == bc && ac == cc) {
            seat.type = FLUSH;
            seat.arr = new int[]{c, b, a};
            return;
        }
        //顺子
        if (a + 1 == b && b + 1 == c) {
            seat.type = STRAIGHT;
            seat.arr = new int[]{c, b, a};
            return;
        } else if (a == 2 && b == 3 && c == 14) {
            seat.type = STRAIGHT;
            if (sf)
                seat.arr = new int[]{3, 2, 1};
            else
                seat.arr = new int[]{c, b, a};
            return;
        }
        //对子
        if (a == b) {
            seat.type = PAIR;
            seat.arr = new int[]{a, c};
            return;
        } else if (a == c) {
            seat.type = PAIR;
            seat.arr = new int[]{a, b};
            return;
        } else if (b == c) {
            seat.type = PAIR;
            seat.arr = new int[]{b, a};
            return;
        }else if (a % 4 != b % 4 && a % 4 != c % 4 && b % 4 != c % 4 &&(a /4 == 2 && b/4==3 && c/4 == 5 )){
            seat.type = SPECIAL;
            seat.arr = new int[]{c,b,a};
            return;
        }
        //单牌
        seat.type = HIGH_CARD;
        seat.arr = new int[]{c, b, a};
    }

    public int allBet(){
        int bet = 0;
        for (Integer tableChip : tableChips)
            bet += tableChip;
        return bet;
    }

    public void initForNext() {
        lastAction = null;
        lastCall = 0;
        lastBlind = false;
        tableChips.clear();
        turn = 0;
        playSeat.clear();
        roundStart = false;
        for (ZjhSeat s:seats){
            if (!s.empty){
                s.ready = false;
                s.lw = 0;
                s.hand.clear();
                s.action = 0;
                s.chips.clear();
                s.seen = false;
                s.sp.clear();
            }
        }
    }

    public void initCard(){
        pokers.clear();
        for (int i = 8; i < 60; i++)
            pokers.add(i);
        //洗牌
        for (int i = 0; i < pokers.size(); i++) {
            int k = RandomUtils.nextInt(pokers.size());
            int temp = pokers.get(i);
            pokers.set(i, pokers.get(k));
            pokers.set(k, temp);
        }
    }
}
