package tdk;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;
import org.apache.commons.lang.ArrayUtils;
import sfs2x.Constant;
import sfs2x.master.Ibase.IExtension;
import sfs2x.master.Ibase.ISeat;
import sfs2x.master.Player;
import sfs2x.master.tdk.TdkSeat;
import sfs2x.master.tdk.TdkTable;
import sfs2x.utils.DBUtil;
import sfs2x.utils.Utils;
import tdk.hander.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class TdkExtension<D extends TdkSeat, T extends TdkTable<D>> extends IExtension<D, T> {
    public static final ReentrantLock GAME_START_LOCK = new ReentrantLock();
    public static final ReentrantReadWriteLock ROUND_START_LOCK = new ReentrantReadWriteLock();
    public static final ReentrantLock RP_LOCK = new ReentrantLock();
    @Override
    public void init() {
        super.init();
        table = Utils.getTable(room);
        addRequestHandler("lr", LeaveRequest.class);
        addRequestHandler("re", ReadyRequest.class);
        addRequestHandler("dis", DisbandRequest.class);
        addRequestHandler("ac", ActionRequest.class);
        addRequestHandler("rp",RepCardHandler.class);
        addRequestHandler("rv",RepHandler.class);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public void startGame() {
        table.initStartGame();
        ISFSObject object = new SFSObject();
        object.putInt("cc", ++table.currentCount);
        object.putIntArray("pot", table.pot);
        send("start", object, room.getUserList());

        //添加底分
        if (table.getPot() == 0) {
            for (D s : table.playSeat) {
                s.bets.add(table.floor);
                table.pot.add(table.floor);
            }

            ISFSObject o = new SFSObject();
            o.putIntArray("pot", table.pot);
            ISFSArray array = new SFSArray();
            for (D s : table.playSeat) {
                ISFSObject ob = new SFSObject();
                ob.putInt("uid", s.player.uid);
                ob.putInt("add", s.bets.get(0));
                ob.putIntArray("bets", s.bets);
                array.addSFSObject(ob);
            }
            o.putSFSArray("u", array);
            send("ab", o, room.getUserList());
        }
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //发牌
        deal();
        table.current = table.getMaxFace();
        table.firstAction = table.current;
        userAction(table.current);
    }

    @Override
    public Object handleInternalMessage(String cmdName, Object params) {
        Player p;
        switch (cmdName) {
            case "jr":
                p = (Player) params;
                requestJoin(p);
                break;
            case "join":
                p = (Player) params;
                reJoin(p);
                break;
            case "p":
                int uid = (int) params;
                return table.getPlayer(uid);
            case "off":
                p = (Player) params;
                offline(p);
        }
        return null;
    }

    private void offline(Player p) {
        D seat = table.getSeat(p);
        if (!seat.empty) {
            seat.offline = true;
            Constant.offlinePlayer.put(p.uid, room);
            ISFSObject object = new SFSObject();
            object.putInt("uid", p.uid);
            send("off", object, room.getUserList());
        }
    }

    private void reJoin(Player p) {
        D seat = table.getSeat(p);
        if (seat == null || !seat.offline)
            return;
        List<User> users = room.getUserList();
        try {
            getApi().joinRoom(p.user, room, null, false, p.user.getLastJoinedRoom());
            Constant.offlinePlayer.remove(p.uid);
            seat.offline = false;
            send("detail", roomDetail(p), p.user);
            if (users.size() > 0) {
                ISFSObject object = new SFSObject();
                object.putInt("uid", p.uid);
                send("on", object, users);
            }
            reconnect(seat);
            if (table.applicant != null)
                sendDisbandInfo(p);
        } catch (SFSJoinRoomException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void requestJoin(Player p) {
        TdkExtension.GAME_START_LOCK.lock();
        int err = 0;
        if (table.cardRoom && table.gameStart) { // 游戏已经开始
            err = 2;
        } else {
            Room room = p.user.getLastJoinedRoom();
            if (room != null && room.isActive()) {
                if (p.room != room) { //在别的房间
                    err = 3;
                } else { // 已经在房间中
                    err = 4;
                }
            } else if (table.curPerson() == table.person)
                err = 5;
        }
        if (err != 0) {
            ISFSObject object = new SFSObject();
            object.putInt("err", err);
            send("jr", object, p.user);
        } else {
            boolean join = true;
            boolean cost = false;
            if (table.cardRoom && table.aa && table.owner != p.uid) {
                if (!DBUtil.lockCard(p.uid, table.need))
                    join = false;
                else
                    cost = true;
            }
            if (!join) { //房卡不足
                ISFSObject object = new SFSObject();
                object.putInt("err", 6);
                send("jr", object, p.user);
            } else {
                try {
                    getApi().joinRoom(p.user, room, null, false, p.user.getLastJoinedRoom());
                    List<User> users = room.getUserList();
                    users.remove(p.user);
                    table.join(p);
                    p.room = room;
                    send("detail", roomDetail(p), p.user);
                    if (users.size() > 0) {
                        ISeat seat = table.getSeat(p);
                        send("join", seat.toSFSObject(), users);
                    }
                    if (table.applicant != null)
                        sendDisbandInfo(p);
                } catch (SFSJoinRoomException e) {
                    if (cost)
                        DBUtil.unLockCard(p.uid, table.need);
                    ISFSObject object = new SFSObject();
                    object.putInt("err", 7);//其他错误,加入失败
                    send("jr", object, p.user);
                    e.printStackTrace();
                }
            }
        }
        TdkExtension.GAME_START_LOCK.unlock();
    }

//    @Override
//    public synchronized void readyRequest(Player p) {
//        super.readyRequest(p);
//        if (table.roundStart) {
//            addTableFloor();
//        }
//    }

    @Override
    protected void reconnect(D seat) {
        if (table.current != null && table.current.action == 1)
            sendAction(table.current, seat);
    }

    private void addTableFloor() {
        for (D s : table.playSeat) {
            s.bets.add(table.floor);
            table.pot.add(table.floor);
        }

        ISFSObject object = new SFSObject();
        object.putInt("f", table.floor);
        object.putIntArray("pot", table.pot);
        ISFSArray array = new SFSArray();
        for (D s : table.playSeat) {
            ISFSObject o = new SFSObject();
            o.putInt("uid", s.player.uid);
            o.putIntArray("bets", s.bets);
            array.addSFSObject(o);
        }
        object.putSFSArray("u", array);
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

    public void userAction(D current) {
        if (table.allFold(current)) { // 只有该玩家可以叫分了,该玩家不用叫分,直接胜出
            table.winner = current;
            showHand();
        } else {
            current.action = 1;
            sendAction(current, null);
        }
    }


    private void sendAction(D current, D seat) {
        ISFSObject object = new SFSObject();
        object.putInt("uid", current.player.uid);
        object.putInt("lid", table.lastAction == null ? 0 : table.lastAction.player.uid);
        object.putInt("lb", table.lastActionBets);
        object.putInt("turn", table.turn);
        object.putIntArray("pot", table.pot);
        if (seat != null)
            send("action", object, seat.player.user);
        else
            send("action", object, room.getUserList());
    }

    public void showHand() {
        if (table.winner != null) {
            for (D seat : table.playSeat) {
                if (seat != table.winner) {
                    int bets = seat.allBet();
                    seat.lw -= bets + seat.drawScore;
                    seat.score -= bets + seat.drawScore;
                    seat.tlw -= bets + seat.drawScore;

                    table.winner.score += bets + seat.drawScore;
                    table.winner.lw += bets + seat.drawScore;
                    table.winner.tlw += bets + seat.drawScore;
                }
            }
        } else { //流局
            for (D seat : table.playSeat) {
                seat.drawScore = seat.allBet();
            }
        }
        ISFSObject object = new SFSObject();
        object.putInt("wid", table.winner == null ? 0 : table.winner.player.uid);
        ISFSArray array = new SFSArray();
        for (D seat : table.playSeat) {
            ISFSObject o = new SFSObject();
            o.putInt("uid", seat.player.uid);
            o.putIntArray("h", seat.hand);
            o.putInt("p", seat.point);
            o.putInt("type", seat.paiType.type.getValue());
            o.putIntArray("arr", Arrays.asList(ArrayUtils.toObject(seat.paiType.arr)));
            o.putInt("lw", seat.lw);
            o.putLong("s", seat.score);
            array.addSFSObject(o);
        }
        object.putSFSArray("u", array);
        send("sh", object, room.getUserList());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (table.currentCount >= table.count) {
            getApi().removeRoom(room);
        } else {
            table.initForNext(table.winner == null);
            send("reGame", null, room.getUserList());
        }

    }

    public void deal() {
        StringBuilder order = new StringBuilder("");
        TdkExtension.RP_LOCK.lock();
        if (table.turn == 1) {
            table.actionOrder.clear();
            D seat = table.getNextAvailable(table.banker);
            int person = table.getAvailableCount();
            table.dealOrder.clear();
            for (int i = 0; i < 3 * person; i++) {
                int num = table.pokers.remove(0);
                seat.hand.add(num);
                if (seat.sendHand.size() < 2)
                    seat.sendHand.add(-1);
                else
                    seat.sendHand.add(num);
                if (i < table.playSeat.size()) {
                    table.dealOrder.add(seat);
                }
                order.append(seat.no);
                if (seat.hand.size() == 3) {
                    seat.point();
                    seat.paiType.getType(seat.hand);
                    seat.sendPaiType.getType(seat.sendHand);
                }
                seat = table.getNextAvailable(seat);
            }

        } else {
            table.dealOrder.clear();
            D seat = table.actionOrder.get(0);
            int person = table.getAvailableCount();
            table.actionOrder.clear();
            for (int i = 0; i < person; i++) {
                int num;
                if (seat.cheat != 0 && table.pokers.contains(seat.cheat)){
                    num = seat.cheat;
                    table.pokers.remove(Integer.valueOf(seat.cheat));
                }else {
                    ArrayList<Integer> temp = new ArrayList<>(table.pokers);
                    temp.removeAll(table.cheatPokers);
                    if (temp.size() > 0){
                        num = temp.remove(0);
                        table.pokers.remove(Integer.valueOf(num));
                    }else {
                        num = table.pokers.remove(0);
                    }
                }
                seat.hand.add(num);
                seat.sendHand.add(num);
                table.dealOrder.add(seat);
                order.append(seat.no);

                seat.point();
                seat.paiType.getType(seat.hand);
                seat.sendPaiType.getType(seat.sendHand);

                seat = table.getNextAvailable(seat);
            }
        }
        TdkExtension.RP_LOCK.unlock();
        //发送给客户端
        for (D is : table.seats) {
            if (!is.empty) {
                ISFSObject object = new SFSObject();
                object.putUtfString("order", String.valueOf(order));
                ISFSArray array = new SFSArray();
                for (D s : table.playSeat) {
                    if (s.action != 4) {
                        ISFSObject o = new SFSObject();
                        o.putInt("uid", s.player.uid);
                        if (is == s) {
                            o.putIntArray("h", s.hand);
                            o.putInt("p", s.point);
                        }else {
                            o.putIntArray("h", s.sendHand);
                            o.putInt("p",s.sendPoint);
                        }
                        array.addSFSObject(o);
                    }
                }
                object.putSFSArray("u", array);
                send("deal", object, is.player.user);
            }
        }
        try {
            Thread.sleep(300*order.length());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public T getTable() {
        return table;
    }
    public void checkBreak() {
        int n = 0;//同意的人数
        int m = 0;//不同意的人数
        int l = 0;//未选择的人数
        for (D s:table.seats){
            if (!s.empty){
                if (s.disbandCode == 0)
                    l++;
                else if (s.disbandCode == 1)
                    n++;
                else if (s.disbandCode == 2)
                    m++;
            }
        }
        if ((100 * n) / table.curPerson() > 50){
            cancelScheduledFuture(autoBreakTask);
            getApi().removeRoom(room);
        }else if ((100 * m)/table.curPerson() >= 50 || l == 0){
            cancelScheduledFuture(autoBreakTask);
            table.applicant = null;
            for (D s:table.seats){
                if (!s.empty)
                    s.disbandCode = 0;
            }
            send("ec",null,room.getUserList());
        }
    }

    /**
     *  发送解散消息
     * @param player 接收消息的玩家,null 表示发送到房间的所有人说
     */
    public void sendDisbandInfo(Player player){
        ISFSObject object = new SFSObject();
        object.putInt("uid", table.applicant.player.uid);
        object.putUtfString("n", table.applicant.player.nick);
        ISFSArray array = new SFSArray();
        for (ISeat s:table.seats){
            if (!s.empty){
                ISFSObject o = new SFSObject();
                o.putInt("uid",s.player.uid);
                o.putInt("e",s.disbandCode);
                array.addSFSObject(o);
            }
        }
        object.putSFSArray("u",array);
        if (player == null)
            send("dis",object,room.getUserList());
        else
            send("dis",object,player.user);
    }

    public void costCard() {
        _task.schedule(new Runnable() {
            @Override
            public void run() {
                Connection conn = DBUtil.getConnection();
                if (conn == null){
                    trace("房间:"+room.getName()+"扣卡失败");
                    return;
                }
                try {
                    if (!table.aa){
                        PreparedStatement stmt = conn.prepareStatement("UPDATE user_info SET card=card-?,lockcard=lockcard-? WHERE userid=? AND card >= ? AND lockcard >= ?");
                        stmt.setInt(1,table.need);
                        stmt.setInt(2,table.need);
                        stmt.setInt(3,table.owner);
                        stmt.setInt(4,table.need);
                        stmt.setInt(5,table.need);
                        if (stmt.executeUpdate() == 1){
                            long card = DBUtil.getCard(table.owner);
                            if (card != -1){
                                ISFSObject object = new SFSObject();
                                object.putInt("uid",table.owner);
                                object.putLong("card",card);
                                getParentZone().getExtension().handleInternalMessage("card",object);
                            }
                            return;
                        }
                        trace("房间:"+room.getName()+",uid:"+table.owner+"扣卡失败");
                    }else {
                        for (D seat:table.playSeat){
                            PreparedStatement stmt = conn.prepareStatement("UPDATE user_info SET card=card-?,lockcard=lockcard-? WHERE userid=? AND card >= ? AND lockcard >= ?");
                            stmt.setInt(1,table.need);
                            stmt.setInt(2,table.need);
                            stmt.setInt(3,table.owner);
                            stmt.setInt(4,table.need);
                            stmt.setInt(5,table.need);
                            if (stmt.executeUpdate() == 1){
                                long card = DBUtil.getCard(seat.player.uid);
                                if (card != -1){
                                    ISFSObject object = new SFSObject();
                                    object.putInt("uid",table.owner);
                                    object.putLong("card",card);
                                    getParentZone().getExtension().handleInternalMessage("card",object);
                                }
                            }else {
                                trace("房间:"+room.getName()+",uid:"+seat.player.uid+"扣卡失败");
                            }
                        }
                    }
                }catch (SQLException e){
                    trace("房间:"+room.getName()+"扣卡失败");
                    e.printStackTrace();
                }finally {
                }
            }
        },0, TimeUnit.MILLISECONDS);
    }
}
