package sfs2x.master.tdk;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import sfs2x.master.Ibase.ISeat;


import java.util.ArrayList;

public class TdkSeat extends ISeat {
    public ArrayList<Integer> hand;
    public ArrayList<Integer> sendHand;
    public ArrayList<Integer> bets;
    public PaiType paiType;
    public PaiType sendPaiType;
    public int point;
    public int sendPoint;
    public int cheat;
    /**
     * 0 默认状态, 1 等待操作状态, 2 跟注 4 弃牌 -1 不踢
     */
    public int action;
    public boolean actioning;
    /**
     * 流局分数
     */
    public int drawScore;
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
        actioning = false;
        paiType = new PaiType(PaiType.Type.invalid,new int[]{0});
        sendPaiType = new PaiType(PaiType.Type.invalid,new int[]{0});
        point = 0;
        sendPoint = 0;
        drawScore = 0;
        cheat = 0;
    }

    @Override
    public void initStartGame() {
        hand.clear();
        sendHand.clear();
        action = 0;
        actioning = false;
        point = 0;
        sendPoint = 0;
        cheat = 0;
    }

    public void initNewTurn() {
        if (action != 4)
            action = 0;
    }

    @Override
    public void userLeave() {
        super.userLeave();
        hand.clear();
        sendHand.clear();
        bets.clear();
        actioning = false;
        action = 0;
        point = 0;
        sendPoint = 0;
        drawScore = 0;
        cheat = 0;
    }
    public void point(){
        int n = 0;
        int m = 0;
        for (int i:hand){
            n+=i/4;
        }
        point = n;
        for (int i:sendHand){
            m+=i/4;
        }
        sendPoint = m;
    }

    public ISFSObject toSFSObject(TdkSeat s) {
        ISFSObject object = toSFSObject();
        object.putBool("isMe",this == s);
        if (this == s) {
            object.putIntArray("h", hand);
            object.putInt("p",point);
        } else {
            object.putIntArray("h", sendHand);
            object.putInt("p",sendPoint);
        }
        object.putIntArray("bets",bets);
        object.putInt("action",action);
        return object;
    }
    public int allBet(){
        int n = 0;
        for (int i:bets)
            n +=i;
        return n;
    }

    public void initForNext(){
        super.initForNext();
        hand.clear();
        sendHand.clear();
        bets.clear();
        paiType = new PaiType(PaiType.Type.invalid,new int[]{0});
        sendPaiType = new PaiType(PaiType.Type.invalid,new int[]{0});
        action = 0;
        point = 0;
        sendPoint = 0;
        actioning = false;
        cheat = 0;
    }
}
