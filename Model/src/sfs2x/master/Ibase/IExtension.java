package sfs2x.master.Ibase;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;
import com.smartfoxserver.v2.extensions.SFSExtension;
import com.smartfoxserver.v2.util.TaskScheduler;
import sfs2x.Constant;
import sfs2x.master.Player;
import sfs2x.utils.DBUtil;
import sfs2x.utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class IExtension extends SFSExtension{
    protected ITable table;
    public Room room;
    private Player disbander = null;
    protected TaskScheduler _task;
    private ScheduledFuture autoBreakTask;
    @Override
    public void init() {
        room = getParentRoom();
        table = Utils.getTable(room);
        _task = SmartFoxServer.getInstance().getTaskScheduler();
    }

    @Override
    public void destroy() {
        super.destroy();
        cancelScheduledFuture(autoBreakTask);
       if (table.cardRoom && !table.takeOff){
           if (table.aa){
               for (ISeat s:table.seats){
                   if (!s.empty)
                       DBUtil.unLockCard(s.player.uid,table.need);
               }
           }else
               DBUtil.unLockCard(table.owner,table.need);
       }
       DBUtil.roomRecord(room);
       if (table.cardRoom && table.gameStart){
           balance();
       }
        for (ISeat seat:table.seats){
            if (!seat.empty) {
                Constant.offlinePlayer.remove(seat.player.uid);
                seat.userLeave();
            }
        }
    }

    private void balance() {
        ISFSObject object = new SFSObject();
        object.putUtfString("name",room.getName());
        ISFSArray array = new SFSArray();
        for (ISeat s:table.seats){
            if (!s.empty){
                ISFSObject o = new SFSObject();
                o.putInt("uid",s.player.uid);
                o.putUtfString("n",s.player.nick);
                if (table.cardRoom)
                    o.putLong("s",s.score);
                else{
                    // TODO: 2018/1/11 金币房
                }
                o.putInt("lw",s.tlw);
                array.addSFSObject(o);
            }
        }
        object.putSFSArray("u",array);
        send("balance",object,room.getUserList());
    }

    /**
     * 请求准备
     * @param p 准备的玩家
     */
    public synchronized void readyRequest(Player p) {
        ISeat seat = table.getSeat(p);
        if (seat == null)
            return;
        if (!table.roundStart){
            if (!seat.ready)
                setReady(seat);
            checkReadyToStart();
        }
    }

    /**
     * 检查准备,是否可以开始游戏
     */
    public void checkReadyToStart(){
        if (table.readyToStart()){
            if (table.cardRoom){
                if (!table.takeOff){
                    if (cost()){
                        table.roundStart = true;
                        table.takeOff = true;
                        _task.schedule(notifyCardUpdate,0,TimeUnit.MILLISECONDS);
                    }else  //扣卡失败
                        getApi().removeRoom(room);
                }else  //已经扣除房卡了
                    table.roundStart = true;
            }else // 金币房
                table.roundStart = true;
        }
        if (table.roundStart){
            table.initStartGame();
            ISFSObject object = new SFSObject();
            object.putInt("c", table.currentCount);
            send("GStart", object, room.getUserList());
            if (!table.gameStart)
                table.gameStart = true;
            for (int i = 0; i < table.playSeat.size(); i++) {
                if (i == table.playSeat.size() - 1)
                    table.playSeat.get(i).next = table.playSeat.get(0);
                else
                    table.playSeat.get(i).next = table.playSeat.get(i + 1);
            }
        }
    }
    /**
     * 设置一个位置为准备状态,并通知所有客户端
     * @param s 座位
     */
    private void setReady(ISeat s) {
        s.ready = true;
        ISFSObject object = new SFSObject();
        object.putInt("uid",s.player.uid);
        send("re",object,room.getUserList());
    }

    /**
     * 玩家加入房间
     * @param p 加入房间的玩家
     */
    protected synchronized void join(Player p) {
        List<User> users = room.getUserList();
        users.remove(p.user);
        if (table.isExists(p)) {//已经在房间中
            ISeat seat = table.getSeat(p);
            seat.offline = false;
            send("detail", roomDetail(p), p.user);
            if (users.size() > 0) {
                ISFSObject object = new SFSObject();
                object.putInt("uid", p.uid);
                send("on", object, users);
            }

            reconnect(seat);

        } else { //新加入房间
            table.join(p);
            p.room = room;
            send("detail", roomDetail(p), p.user);
            if (users.size() > 0) {
                ISeat seat =  table.getSeat(p);
                send("join", seat.toSFSObject(), users);
            }
        }
        if (disbander != null)
            sendDisbandInfo(p);
    }

    protected abstract void reconnect(ISeat seat);

    private ISFSObject roomDetail(Player p){
        ISFSObject object = table.toSFSObject(p);
        object.putUtfString("name",room.getName());
        return object;
    }
    /**
     *  请求加入房间
     * @param p 加入房间的玩家
     */
    protected synchronized void requestJoin(Player p){
        ISFSObject object = new SFSObject();
        if (table.cardRoom && table.gameStart){ // 房卡房游戏已经开始
            object.putInt("err",2);
            send("jr",object,p.user);
        }else if (p.room != null && p.room != room){ //已经在别的房间了
            object.putInt("err",3);
            send("jr",object,p.user);
        }else if (room.containsUser(p.user)){ //已经在房间里面了
            object.putInt("err",4);
            send("jr",object,p.user);
        }else if (table.person == table.curPerson()){ //房间满了
            object.putInt("err",5);
            send("jr",object,p.user);
        }else {
            boolean join = true;
            boolean cost = false;
            if (table.cardRoom && table.aa && table.owner != p.uid){
                if (!DBUtil.lockCard(p.uid,table.need)) {
                    join = false;
                }else
                    cost = true;
            }
            if (!join){
                object.putInt("err",6);//房卡不足
                send("jr",object,p.user);
            }else {
                try {
                    getApi().joinRoom(p.user,room,null,false,p.user.getLastJoinedRoom());
                    object.putInt("err",0);//加入成功
                    send("jr",object,p.user);
                    join(p);
                } catch (SFSJoinRoomException e) {
                    e.printStackTrace();
                    if (cost)
                        DBUtil.unLockCard(p.uid,table.need);
                    object.putInt("err",7);//其他错误,加入失败
                    send("jr",object,p.user);
                }
            }
        }
    }

    /**
     * 请求离开房间
     * @param p 玩家
     */
    public synchronized boolean leaveRequest(Player p){
        final ISeat seat = table.getSeat(p);
        boolean leaved = false;
        if (seat != null){
            if (table.cardRoom){
                if (!table.gameStart){ //房间未开局并且玩家不在当前游戏中,直接退出房间
                    getApi().leaveRoom(p.user,room);
                    Constant.offlinePlayer.remove(p.uid);
                    seat.userLeave();
                    leaved = true;
                    ISFSObject object = new SFSObject();
                    object.putInt("uid",p.uid);
                    send("leave",object,p.user);
                    send("leave",object,room.getUserList());
                    if (table.curPerson() == 0)
                        getApi().removeRoom(room);
                    else {
                        checkReadyToStart();
                    }
                }else {
                    if (disbander == null){
                        disbander = p;
                        seat.disbandCode = 1;
                        sendDisbandInfo(null);
                        autoBreakTask = _task.schedule(new Runnable() {
                            @Override
                            public void run() {
                                synchronized (IExtension.this){
                                    for (ISeat s:table.seats){
                                        if (!s.empty && s.disbandCode == 0)
                                            s.disbandCode = 1;
                                    }
                                    checkBreak();
                                }
                            }
                        },5*60000, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }
        // TODO: 2018/1/13
        return leaved;
    }

    /**
     * 检查申请解散的结果
     */
    private void checkBreak() {
        int n = 0;//同意的人数
        int m = 0;//不同意的人数
        int l = 0;//未选择的人数
        for (ISeat s:table.seats){
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
            disbander = null;
            for (ISeat s:table.seats){
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
    private void sendDisbandInfo(Player player){
        ISFSObject object = new SFSObject();
        object.putInt("uid", disbander.uid);
        object.putUtfString("n", disbander.nick);
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

    /**
     * 处理申请解散,选择同意或拒绝
     * @param p 发送请求的玩家
     */
    public synchronized void disbandRequest(Player p,boolean e) {
        ISeat seat = table.getSeat(p);
        if (seat != null && disbander != null){
            if (!seat.empty){
                if (seat.disbandCode == 0){
                    if (e)
                        seat.disbandCode = 1;
                    else
                        seat.disbandCode = 2;
                    sendDisbandInfo(null);
                    checkBreak();
                }
            }
        }
    }

    protected void cancelScheduledFuture(ScheduledFuture scheduledFuture) {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(false);
        }
    }

    private boolean cost(){
        Connection conn = DBUtil.getConnection();
        if (conn == null)
            return false;
        try {
            if (!table.aa) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE user_info SET card = card - ?,lockcard = lockcard - ? WHERE userid=? AND card >= ? AND lockcard >= ?");
                stmt.setInt(1,table.need);
                stmt.setInt(2,table.need);
                stmt.setInt(3,table.owner);
                stmt.setInt(4,table.need);
                stmt.setInt(5,table.need);
                int n = stmt.executeUpdate();
                stmt.close();
                return n == 1;
            }else {
                conn.setAutoCommit(false);
                for (ISeat s:table.playSeat){
                    PreparedStatement stmt = conn.prepareStatement("UPDATE user_info SET card = card - ?,lockcard = lockcard - ? WHERE userid=? AND card >= ? AND lockcard >= ?");
                    stmt.setInt(1,table.need);
                    stmt.setInt(2,table.need);
                    stmt.setInt(3,s.player.uid);
                    stmt.setInt(4,table.need);
                    stmt.setInt(5,table.need);
                    int n = stmt.executeUpdate();
                    stmt.close();
                    if (n != 1) {
                        conn.rollback();
                        return false;
                    }
                }
                conn.commit();
                return true;
            }
        }catch (SQLException e){
            return false;
        }finally {
            DBUtil.close(conn,null,null);
        }
    }

    private Runnable notifyCardUpdate = new Runnable() {
        @Override
        public void run() {
            if (!table.aa){
                long card = DBUtil.getCard(table.owner);
                if (card != -1){
                    ISFSObject object = new SFSObject();
                    object.putInt("uid",table.owner);
                    object.putLong("card",card);
                    getParentZone().getExtension().handleInternalMessage("card",object);
                }

            }else {
                for (ISeat s:table.playSeat){
                    long card = DBUtil.getCard(s.player.uid);
                    if (card != -1 && card != s.player.card){
                        ISFSObject object = new SFSObject();
                        object.putInt("uid",table.owner);
                        object.putLong("card",card);
                        getParentZone().getExtension().handleInternalMessage("card",object);
                    }
                }
            }
        }
    };
}
