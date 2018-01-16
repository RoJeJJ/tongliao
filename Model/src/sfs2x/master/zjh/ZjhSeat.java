package sfs2x.master.zjh;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import sfs2x.master.Ibase.ISeat;

import java.util.ArrayList;

public class ZjhSeat extends ISeat {
    public ArrayList<Integer> hand;
    public int type;
    public int[] arr;
    public boolean seen;
    public ArrayList<ZjhSeat> sp;
    /**
     *  0 默认状态,1 下注状态,2跟注,3 加注,4,弃牌,5,比牌输了,6,比牌赢了
     */
    public int action;
    public ArrayList<Integer> chips;
    public ZjhSeat(int no) {
        super(no);
        hand = new ArrayList<>();
        chips = new ArrayList<>();
        action = 0;
        sp = new ArrayList<>();
    }

    @Override
    public void initStartGame() {
        hand.clear();
        seen = false;
        chips.clear();
        action = 0;
    }

    public ISFSObject toSFSObject(ZjhSeat s) {
        ISFSObject object = toSFSObject();
        object.putBool("isMe",this == s);
        object.putBool("seen",seen);
        object.putIntArray("bets",chips);
        object.putInt("action",action);
        object.putBool("hh",hand.size() > 0);
        if (seen && this == s) {
            object.putIntArray("h", hand);
            object.putInt("type",type);
        }
        return object;
    }

    public void initNewTurn() {
        action = 0;
    }

    public int allBet(){
        int bet = 0;
        for (int b : chips)
            bet += b;
        return bet;
    }
}
