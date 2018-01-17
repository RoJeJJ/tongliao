package sfs2x.master.tdk;

import sfs2x.master.Ibase.ISeat;

import java.util.ArrayList;

public class TdkSeat extends ISeat {
    public ArrayList<Integer> hand;
    public ArrayList<Integer> sendHand;
    public ArrayList<Integer> bets;
    /**
     * 0 默认状态, 1 等待操作状态, 2 跟注, 3下注, 4 弃牌
     */
    public int action;
    /**
     * 构造方法
     *
     * @param no 座位号
     */
    public TdkSeat(int no) {
        super(no);
        hand = new ArrayList<>();
        sendHand = new ArrayList<>();
        bets = new ArrayList<>();
        action = 0;
    }

    @Override
    public void initStartGame() {
        hand.clear();
        sendHand.clear();
        action = 0;
    }

    public void initNewTurn() {
        if (action != 4)
            action = 0;
    }
}
