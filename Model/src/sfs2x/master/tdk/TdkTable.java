package sfs2x.master.tdk;

import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import org.apache.commons.lang.math.RandomUtils;
import sfs2x.master.Ibase.ITable;
import sfs2x.master.Player;

import java.util.ArrayList;

public class TdkTable<T extends TdkSeat> extends ITable<T> {
    public final int floor;
    public ArrayList<Integer> pot;
    public T current;
    public T banker;
    public int turn;
    public T winner;
    public ArrayList<Integer> pokers;
    public ArrayList<T> dealOrder;
    public ArrayList<T> actionOrder;
    public T firstAction;
    public int lastActionBets;
    public T lastAction;
    public ArrayList<T> tiOrder;
    public int tiIndex;
    public T applicant;
    public ArrayList<T> buTi;
    public ArrayList<Integer> cheatPokers;

    /**
     * 房卡房构造方法
     *
     * @param mod      游戏类型
     * @param aa AA制
     * @param cardRoom 房卡房
     * @param owner 房主
     * @param person 人数
     * @param count 局数
     * @param need 需要的房卡数
     */
    @SuppressWarnings("unchecked")
    public TdkTable(int mod, boolean aa, boolean cardRoom, int owner, int person, int count, int need) {
        super(mod, aa, cardRoom, owner, person, count, need);
        seats = (T[]) new TdkSeat[person];
        for (int i=0;i<person;i++)
            seats[i] = (T) new TdkSeat(i);
        floor = 1;
        pot = new ArrayList<>();
        current = null;
        turn = 0;
        pokers = new ArrayList<>();
        dealOrder = new ArrayList<>();
        actionOrder = new ArrayList<>();
        firstAction = null;
        lastActionBets = 0;
        lastAction = null;
        applicant = null;
        tiOrder = new ArrayList<>();
        buTi = new ArrayList<>();
        cheatPokers = new ArrayList<>();
    }

    @Override
    public boolean readyToStart() {
        playSeat.clear();
        for (T s:seats){
            if (!s.empty && s.ready)
                playSeat.add( s);
        }
        return playSeat.size() >= 2 && playSeat.size() == curPerson();
    }

    @Override
    public void initStartGame() {
        pot.clear();
        current = null;
        turn = 1;
        pokers.clear();
        firstAction = null;
        record = new StringBuffer("|");
        lastAction = null;
        lastActionBets = 0;
        //初始化牌
        for (int f:PaiType.face){
            if (f == 6){
                pokers.add(f*4);//方块6
                pokers.add(f*4+2);//红桃6
            }else if (f == 15 || f == 16) { //大小王
                pokers.add(f * 4);
            }else{
                pokers.add(f*4);//方
                pokers.add(f*4+1);//梅
                pokers.add(f*4+2);//红
                pokers.add(f*4+3);//黑
            }
        }

        //洗牌
        for (int i = 0; i < pokers.size(); i++) {
            int k = RandomUtils.nextInt(pokers.size());
            int temp = pokers.get(i);
            pokers.set(i, pokers.get(k));
            pokers.set(k, temp);
        }
        if (winner != null && playSeat.contains(winner))
            banker = winner;
        else {
            int index = RandomUtils.nextInt(playSeat.size());
            banker =  playSeat.get(index);
        }
    }

    public T getNextAvailable(T seat){
        T next = seat;
        do{
            int index = playSeat.indexOf(next);
            next = index+1 == playSeat.size()?playSeat.get(0):playSeat.get(index+1);
        }while (next.action == 4);
        return next;
    }
    public int getAvailableCount(){
        int n = 0;
        for (T s:playSeat){
            if (s.action != 4)
                n++;
        }
        return n;
    }

    public boolean allFold(T seat){
        for (T s:playSeat){
            if (s != seat && s.action != 4)
                return false;
        }
        return true;
    }
    public boolean noBody_Ti(){
        for (T s:tiOrder){
            if (s.action != 4 && s.action != -1)
                return false;
        }
        return true;
    }

    public boolean allAction(){
        for (T s:playSeat){
            if (s.action != 2 && s.action != 4)
                return false;
        }
        return true;
    }

    public T getWinner(){
        ArrayList<T> winners = new ArrayList<>();
        for (T seat : playSeat) {
            if (seat.action != 4) {
                if (winners.size() == 0) {
                    winners.add(seat);
                } else {
                    T last = winners.get(winners.size() - 1);
                    if (seat.paiType.type.getValue() > last.paiType.type.getValue()) {
                        winners.clear();
                        winners.add(seat);
                    } else if (seat.paiType.type.getValue() == last.paiType.type.getValue()) {
                        T t = arrC(seat, last);
                        if (t == seat) {
                            winners.clear();
                            winners.add(seat);
                        }else if (t == null)
                            winners.add(seat);
                    }
                }
            }
        }
        if (winners.size() == 1)
            return winners.get(0);
        else
            return null;
    }

    private T arrC(T t1,T t2){
        for (int i=0;i<t1.paiType.arr.length;i++){
            if (t1.paiType.arr[i] > t2.paiType.arr[i])
                return t1;
            else if (t1.paiType.arr[i] < t2.paiType.arr[i])
                return t2;
        }
        return null;
    }

    public T getMaxFace(){
        T max = null;
        for (T t : dealOrder) {
            if (t.action != 4) {
                if (max == null)
                    max = t;
                else if (t.hand.get(t.hand.size() - 1) / 4 > max.hand.get(max.hand.size() - 1) / 4)
                    max = t;
            }
        }
        return max;
    }
    public  void startNewTurn() {
        turn++;
        for (T s:playSeat){
            s.initNewTurn();
        }
    }
    public int getPot(){
        int n = 0;
        for (int p:pot)
            n += p;
        return n;
    }

    @Override
    public ISFSObject toSFSObject(Player p) {
        T seat = getSeat(p);
        ISFSObject object =  super.toSFSObject(p);
        object.putInt("f",floor);
        object.putIntArray("pot",pot);
        object.putInt("turn",turn);
        ISFSArray array = new SFSArray();
        for (T s:seats){
            if (!s.empty){
                ISFSObject o = s.toSFSObject(seat);
                array.addSFSObject(o);
            }
        }
        object.putSFSArray("u",array);
        return object;
    }

    /**
     * 准备下局游戏,初始化牌局
     * @param draw 本局是否烂锅
     */
    public void initForNext(boolean draw) {
        current = null;
        dealOrder.clear();
        actionOrder.clear();
         firstAction = null;
         lastActionBets = 0;
         lastAction = null;
         tiOrder.clear();
         buTi.clear();
         tiIndex = 0;
         cheatPokers.clear();
         if (!draw)
             pot.clear();
         for (T s:playSeat) {
             s.initForNext();
         }
         super.initForNext();
    }
}
