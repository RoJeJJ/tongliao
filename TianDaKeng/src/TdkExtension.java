
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import hander.ActionRequest;
import hander.DisbandRequest;
import hander.LeaveRequest;
import hander.ReadyRequest;
import sfs2x.master.Ibase.IExtension;
import sfs2x.master.Ibase.ISeat;
import sfs2x.master.Player;
import sfs2x.master.tdk.TdkSeat;
import sfs2x.master.tdk.TdkTable;
import sfs2x.master.zjh.ZjhSeat;

public class TdkExtension extends IExtension {
    private TdkTable table;
    @Override
    public void init() {
        table = (TdkTable) super.table;
        addRequestHandler("lr", LeaveRequest.class);
        addRequestHandler("re", ReadyRequest.class);
        addRequestHandler("dis", DisbandRequest.class);
        addRequestHandler("ac", ActionRequest.class);
    }

    @Override
    protected void reconnect(ISeat seat) {

    }

    @Override
    public Object handleInternalMessage(String cmdName, Object params) {
        switch (cmdName){
            case "jr":
                Player p = (Player) params;
                requestJoin(p);
                break;
        }
        return null;
    }

    @Override
    public synchronized void readyRequest(Player p) {
        super.readyRequest(p);
        if (table.roundStart){
            addTableFloor();
        }
    }

    private void addTableFloor() {
        for (ISeat s:table.playSeat){
            TdkSeat seat = (TdkSeat) s;
            seat.bets.add(table.floor);
            table.bets.add(table.floor);
        }

        ISFSObject object = new SFSObject();
        object.putInt("f", table.floor);
        object.putIntArray("chip",table.bets);
        ISFSArray array = new SFSArray();
        for (ISeat s:table.playSeat){
            ISFSObject o = new SFSObject();
            ZjhSeat seat = (ZjhSeat) s;
            o.putInt("uid",s.player.uid);
            o.putIntArray("bets",seat.chips);
            array.addSFSObject(o);
        }
        object.putSFSArray("u",array);
        send("add", object, room.getUserList());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        deal();
        table.current = table.getMaxFace();
        table.firstAction = table.current;
        userAction(table.current);
    }

    private void userAction(TdkSeat current) {
        if (current == table.getNextAvailable(current)){ // 只有该玩家可以叫分了,该玩家不用叫分,直接胜出
            table.winner = current;
            showHand();
        }else if (current.action == 0){
            current.action = 1;
            sendAction(current,null);
        }
    }

    private void sendAction(TdkSeat current, TdkSeat seat) {
        ISFSObject object = new SFSObject();
        object.putInt("uid",current.player.uid);
    }

    private void showHand() {
        // TODO: 2018/1/16 检视玩家手牌
    }

    private void deal() {
        if (table.turn == 0){
            TdkSeat seat = (TdkSeat) table.banker.next;
            table.dealOrder.clear();
            for (int i=0;i<3*table.playSeat.size();i++){
                int num = table.pokers.remove(0);
                seat.hand.add(0,num);
                if (seat.hand.size() < 2)
                    seat.sendHand.add(0,-1);
                else
                    seat.sendHand.add(0,num);
                if (i < table.playSeat.size())
                    table.dealOrder.add(seat);
                seat = (TdkSeat) seat.next;
            }
        }else {
            table.dealOrder.clear();
            TdkSeat seat = table.actionOrder.get(0);
            for (int i=0;i<table.playSeat.size();i++){
                int num = table.pokers.remove(0);
                seat.hand.add(0,num);
                seat.sendHand.add(0,num);
                table.dealOrder.add(seat);
                seat = table.getNextAvailable(seat);
            }
        }
        // TODO: 2018/1/16 发送手牌
    }
}
