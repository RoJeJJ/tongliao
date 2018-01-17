package sfs2x.master.tdk;

import org.apache.commons.lang.math.RandomUtils;
import sfs2x.master.Ibase.ISeat;
import sfs2x.master.Ibase.ITable;

import java.util.ArrayList;

public class TdkTable extends ITable {
    public final int floor;
    public ArrayList<Integer> bets;
    public TdkSeat current;
    public TdkSeat banker;
    public int turn;
    public TdkSeat winner;
    public ArrayList<Integer> pokers;
    public ArrayList<TdkSeat> dealOrder;
    public ArrayList<TdkSeat> actionOrder;
    public TdkSeat firstAction;
    public int lastActionBets;
    public TdkSeat lastAction;
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
    public TdkTable(int mod, boolean aa, boolean cardRoom, int owner, int person, int count, int need) {
        super(mod, aa, cardRoom, owner, person, count, need);
        seats = new TdkSeat[person];
        for (int i=0;i<person;i++)
            seats[i] = new TdkSeat(i);
        floor = 1;
        bets = new ArrayList<>();
        current = null;
        turn = 0;
        pokers = new ArrayList<>();
        dealOrder = new ArrayList<>();
        actionOrder = new ArrayList<>();
        firstAction = null;
    }

    @Override
    public boolean readyToStart() {
        playSeat.clear();
        for (ISeat s:seats){
            if (!s.empty && s.ready)
                playSeat.add(s);
        }
        return playSeat.size() >= 2 && playSeat.size() == curPerson();
    }

    @Override
    public void initStartGame() {
        bets.clear();
        current = null;
        turn = 0;
        pokers.clear();
        firstAction = null;
        record = new StringBuffer("|");
        //初始化牌
        int[] face = new int[]{7,8,9,10,11,12,13,14};
        for (int f:face){
            pokers.add(f*4);//方
            pokers.add(f*4+1);//梅
            pokers.add(f*4+2);//红
            pokers.add(f*4+3);//黑
        }
        pokers.add(6*4);//6
        pokers.add(6*4+2);//6
        pokers.add(15*4);//小王
        pokers.add(16*4);//大王
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
            banker = (TdkSeat) playSeat.get(index);
        }
    }

    public TdkSeat getNextAvailable(TdkSeat seat){
        TdkSeat next = (TdkSeat) seat.next;
        while (next.action == 4)
            next = (TdkSeat) next.next;
        return next;
    }

    public TdkSeat getMaxFace(){
        TdkSeat max = dealOrder.get(0);
        for (int i=1;i<dealOrder.size();i++){
            if (dealOrder.get(i).hand.get(0)/4 > max.hand.get(0) / 4)
                max = dealOrder.get(i);
        }
        return max;
    }

    public void startNewTurn() {
        turn++;
        actionOrder.clear();
        for (ISeat s:playSeat){
            TdkSeat seat = (TdkSeat) s;
            seat.initNewTurn();
        }
    }
}
