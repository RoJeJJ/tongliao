package zjh;

import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;
import org.apache.commons.lang.math.RandomUtils;
import sfs2x.Constant;
import sfs2x.master.Ibase.IExtension;
import sfs2x.master.Ibase.ISeat;
import sfs2x.master.Player;
import sfs2x.master.zjh.ZjhSeat;
import sfs2x.master.zjh.ZjhTable;
import sfs2x.utils.DBUtil;
import zjh.handler.*;

import java.util.concurrent.ScheduledFuture;

/**
 * 扎金花游戏扩展
 */
public class ZjhExtension extends IExtension {
    private ZjhTable table;
    private ScheduledFuture actionScheduled = null;
    private long actionRemainTime;
    private long actionStartTime;
    private static final long ACTION_WAIT_TIME = 20 * 1000;
    @Override
    public void init() {
        super.init();
        table = (ZjhTable) super.table;
        addRequestHandler("re", ReadyRequest.class);
        addRequestHandler("lr", LeaveRoomRequest.class);
        addRequestHandler("dis", DisbandRequest.class);
        addRequestHandler("f", FollowRequest.class);
        addRequestHandler("r", RaiseRequest.class);
        addRequestHandler("fo",FoldRequest.class);
        addRequestHandler("sh",ShowCardRequest.class);
        addRequestHandler("vs",VSRequest.class);
        addRequestHandler("v",HandRequest.class);
    }


    @Override
    public Object handleInternalMessage(String cmdName, Object params) {
        if (cmdName.equals("jr")) {
            Player player = (Player) params;
            requestJoin(player);
        }else if (cmdName.equals("off")){
            Player p = (Player) params;
                offline(p);
        }else if (cmdName.equals("p")){
            int uid = (int) params;
            return table.getPlayer(uid);
        }else if (cmdName.equals("join")){
            synchronized (this){
                Player player = (Player) params;
                try {
                    getApi().joinRoom(player.user, room, null,false, player.user.getLastJoinedRoom());
                    join(player);
                }catch (SFSJoinRoomException e){
                    e.printStackTrace();
                }
            }
        }
//        else if (cmdName.equals("leave")){
//            Player player = (Player) params;
//            leaveRoom(player);
//        }

        return null;
    }
    private synchronized void offline(Player p){
        ZjhSeat seat = (ZjhSeat) table.getSeat(p);
        if (seat != null){
            seat.offline = true;
            Constant.offlinePlayer.put(p.uid,room);
            ISFSObject object = new SFSObject();
            object.putInt("uid",p.uid);
            send("off",object,room.getUserList());
        }
    }


    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void readyRequest(Player p) {
        super.readyRequest(p);
        if (table.roundStart) {
            table.initStartGame();
            ISFSObject object = new SFSObject();
            object.putInt("c", table.currentCount);
            send("GStart", object, room.getUserList());

            addTableFloor();
        }
    }

    @Override
    protected void reconnect(ISeat seat) {
        if (table.current != null && table.current.action == 1) {
            actionRemainTime = ACTION_WAIT_TIME - (System.currentTimeMillis() - actionStartTime);
            sendAction(table.current, (ZjhSeat) seat);
        }
    }

    private void addTableFloor() {
        for (ISeat s : table.playSeat) {
            ZjhSeat seat = (ZjhSeat) s;
            seat.chips.add(table.floor);
            table.tableChips.add(table.floor);
        }
        ISFSObject object = new SFSObject();
        object.putInt("f", table.floor);
        object.putIntArray("chip",table.tableChips);
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
        table.current = (ZjhSeat) table.banker.next;
        userAction(table.current);
    }

    /**
     * 发牌
     */
    private void deal() {
        StringBuilder order = new StringBuilder();
        ZjhSeat seat = (ZjhSeat) table.banker.next;
        for (int i = 0; i < 3 * table.playSeat.size(); i++) {
            int index = RandomUtils.nextInt(table.pokers.size());
            int pokerNum = table.pokers.remove(index);
            seat.hand.add(pokerNum);
            order.append(seat.no);
            seat = (ZjhSeat) seat.next;
        }
        ISFSObject object = new SFSObject();
        object.putUtfString("order", order.toString());
        send("hand", object, room.getUserList());
        for (ISeat s : table.playSeat)
            table.analyzeType((ZjhSeat) s);
        try {
            Thread.sleep(200 * table.playSeat.size() * 3 + 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void userAction(ZjhSeat s) {
        if (s == table.getNextActionSeat(s)) { //场上只有一个玩家了,该玩家胜出
            table.winner = s;
            showHand();
        } else if (s.action != 0) {
            startNewTurn();
            s.action = 1;
            actionRemainTime = ACTION_WAIT_TIME;
            actionStartTime = System.currentTimeMillis();
            sendAction(s,null);
//            actionScheduled = _task.schedule(new Runnable() {
//                @Override
//                public void run() {
//                    foldRequest(table.current.player);
//                }
//            },(int) ACTION_WAIT_TIME, TimeUnit.MILLISECONDS);
        } else {
            s.action = 1;
            actionRemainTime = ACTION_WAIT_TIME;
            actionStartTime = System.currentTimeMillis();
            sendAction(s,null);
//            actionScheduled = _task.schedule(new Runnable() {
//                @Override
//                public void run() {
//                    foldRequest(table.current.player);
//                }
//            },(int) ACTION_WAIT_TIME, TimeUnit.MILLISECONDS);
        }
    }

    private void showHand() {
        for (ISeat s:table.playSeat){
            ZjhSeat seat = (ZjhSeat) s;
            ISFSObject object = new SFSObject();
            object.putInt("oid",table.winner.player.uid);
            ISFSArray array = new SFSArray();
            for (ZjhSeat se:seat.sp){
                ISFSObject o = new SFSObject();
                o.putInt("uid",se.player.uid);
                o.putIntArray("hand",se.hand);
                array.addSFSObject(o);
            }
            if (seat.sp.size() > 0){
                ISFSObject o = new SFSObject();
                o.putInt("uid",s.player.uid);
                o.putIntArray("hand", seat.hand);
                array.addSFSObject(o);
            }
            object.putSFSArray("u",array);
            send("show",object,s.player.user);
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        settlement();
    }

    private void settlement(){
        for (ISeat s:table.playSeat){
            ZjhSeat seat = (ZjhSeat) s;
            if (seat != table.winner){
                int bet = seat.allBet();
                seat.score -= bet;
                seat.lw -= bet;
                seat.tlw -= bet;
                table.winner.score += bet;
                table.winner.lw += bet;
                table.winner.tlw += bet;
            }
        }
        sendSettle();

        if (table.cardRoom){
            if (table.currentCount >= table.count)
                getApi().removeRoom(room);
            else
                table.initForNext();
        }
    }

    private void sendSettle() {
        ISFSObject object = new SFSObject();
        object.putInt("wid",table.winner.player.uid);
        ISFSArray array = new SFSArray();
        for (ISeat s: table.playSeat){
            ISFSObject o = new SFSObject();
            o.putInt("uid",s.player.uid);
            if (table.cardRoom){
                o.putLong("s",s.score);
            }else {
                // TODO: 2018/1/9 金币房
            }
            o.putInt("lw",s.lw);
            array.addSFSObject(o);
        }
        object.putSFSArray("u",array);

        send("settle",object,room.getUserList());
    }

    private void startNewTurn() {
        table.turn++;
        for (ISeat s : table.playSeat) {
            ZjhSeat seat = (ZjhSeat) s;
            if (seat.action != 4 && seat.action != 5)
                seat.initNewTurn();
        }
    }

    private void sendAction(ZjhSeat seat,ZjhSeat s) {
        ISFSObject object = new SFSObject();
        object.putInt("uid", seat.player.uid);
        object.putInt("turn", table.turn);
//        object.putLong("time", actionRemainTime);
        object.putInt("lc", table.lastCall);
        object.putBool("lb", table.lastBlind);
        object.putBool("see", !seat.seen && table.turn > table.men);//看牌
        object.putBool("vs", table.turn > 3);//比牌
        object.putBool("follow", table.turn < ZjhTable.MAX_TURN);//跟注
        object.putBool("raise", table.turn < ZjhTable.MAX_TURN);//加注
        object.putBool("fold", seat.action != 4 && seat.action != 5);//弃牌
        if (s == null)
            send("call", object, room.getUserList());
        else
            send("call", object, s.player.user);
    }


    /**
     * 跟注
     * @param p 玩家
     */
    public synchronized void followRequest(Player p) {
        ZjhSeat seat = (ZjhSeat) table.getSeat(p);
        if (seat != null && table.roundStart && seat.action == 1 && table.turn <= ZjhTable.MAX_TURN) {
            if (table.cardRoom) { //房卡房
                if (seat.seen) { //看牌了
                    if (table.lastCall == 0) {
                        seat.chips.add(table.floor);
                        table.tableChips.add(table.floor);
                        table.lastCall = table.floor;
                        table.lastBlind = false;
                        seat.action = 2;
                        table.lastAction = seat;
                    } else {
                        if (table.lastBlind) {
                            seat.chips.add(table.lastCall * 2);
                            table.tableChips.add(table.lastCall * 2);
                            table.lastCall = table.lastCall * 2;
                            table.lastBlind = false;
                            seat.action = 2;
                            table.lastAction = seat;
                        } else {
                            seat.chips.add(table.lastCall);
                            table.tableChips.add(table.lastCall);
                            seat.action = 2;
                            table.lastAction = seat;
                        }
                    }
                } else { //没看牌
                    if (table.lastCall == 0) {
                        seat.chips.add(table.floor);
                        table.tableChips.add(table.floor);
                        table.lastBlind = true;
                        table.lastCall = table.floor;
                        seat.action = 2;
                        table.lastAction = seat;
                    } else {
                        if (table.lastBlind) {
                            seat.chips.add(table.lastCall);
                            table.tableChips.add(table.lastCall);
                            seat.action = 2;
                            table.lastAction = seat;
                        } else {
                            int chip = table.lastCall / 2 == 0?1:table.lastCall / 2;
                            seat.chips.add(chip);
                            table.tableChips.add(chip);
                            table.lastCall = chip;
                            table.lastBlind = true;
                            seat.action = 2;
                            table.lastAction = seat;
                        }
                    }
                }
            } else {
                // TODO: 2018/1/9 金币房
            }
            if (seat.action == 2) {
                sendActionResult(seat);
                cancelScheduledFuture(actionScheduled);

                table.current = table.getNextActionSeat(seat);
                userAction(table.current);
            }
        }
    }

    /**
     * 弃牌
     * @param p 玩家的位置
     */
    public synchronized void foldRequest(Player p) {
        ZjhSeat seat = (ZjhSeat) table.getSeat(p);
        if (seat != null && table.roundStart) {
            if (seat.action != 4 && seat.action != 5 && table.playSeat.contains(seat)) {
                seat.action = 4;
                sendActionResult(seat);
                if (table.current == seat) { // 弃牌的玩家处于action状态
                    cancelScheduledFuture(actionScheduled);

                    table.current = table.getNextActionSeat(seat);
                    userAction(table.current);
                } else if (table.current.action == 1){ // 弃牌后只剩一个玩家,直接胜出
                    if (table.current == table.getNextActionSeat(table.current)){
                        cancelScheduledFuture(actionScheduled);
                        table.winner = table.current;
                        showHand();
                    }
                }
            }
        }
    }

    /**
     * 加注
     *
     * @param p 玩家
     * @param coin 加注的分数
     */
    public synchronized void raiseRequest(Player p, int coin) {
        ZjhSeat seat = (ZjhSeat) table.getSeat(p);
        if (seat != null && table.roundStart && table.turn <= ZjhTable.MAX_TURN && seat.action == 1){
            if (table.cardRoom){ //房卡房
                if (table.lastCall == 0){ //还没有人下注
                    if (coin - table.floor > 0 && coin % table.floor == 0){
                        table.lastBlind = !seat.seen;
                        seat.action = 3;
                    }
                }else {
                    if (seat.seen){
                        if (!table.lastBlind && coin - table.lastCall > 0 && (coin - table.lastCall) % table.floor == 0){
                            table.lastBlind = false;
                            seat.action = 3;
                        }else if (table.lastBlind && coin / 2 - table.lastCall> 0 && (coin / 2 - table.lastCall) % table.floor == 0){
                            table.lastBlind = false;
                            seat.action = 3;
                        }
                    }else {
                        if (!table.lastBlind && coin - table.lastCall / 2 > 0 && (coin - table.lastCall / 2 ) % table.floor == 0) {
                            table.lastBlind = true;
                            seat.action = 3;
                        }
                        if (table.lastBlind && coin - table.lastCall > 0 && (coin - table.lastCall) % table.floor == 0) {
                            table.lastBlind = true;
                            seat.action = 3;
                        }
                    }
                }
                if (seat.action == 3){
                    seat.chips.add(coin);
                    table.tableChips.add(coin);
                    table.lastCall = coin;
                    table.lastAction = seat;
                    sendActionResult(seat);

                    cancelScheduledFuture(actionScheduled);

                    table.current = table.getNextActionSeat(seat);
                    userAction(table.current);

                }
            }else {
                // TODO: 2018/1/9 金币房
            }
        }
    }


    private void sendActionResult(ZjhSeat seat) {
        ISFSObject object = new SFSObject();
        object.putInt("uid", seat.player.uid);
        object.putInt("action", seat.action);
        if (table.cardRoom)
            object.putLong("s", seat.score);
        else
            object.putLong("s", seat.player.card);
        object.putIntArray("chip",seat.chips);
        object.putInt("lc", table.lastCall);
        object.putBool("lb", table.lastBlind);
        object.putInt("ab", table.allBet());
        send("action", object, room.getUserList());
    }

    /**
     * 看牌
     * @param p 玩家
     */
    public synchronized void showCardRequest(Player p) {
        ZjhSeat seat = (ZjhSeat) table.getSeat(p);
        if (seat != null && table.roundStart && !seat.seen && seat.hand.size() == 3){
            seat.seen = true;
            sendHand(seat);

            notifySeen(seat);
        }
    }

    private void notifySeen(ZjhSeat seat) {
        ISFSObject object = new SFSObject();
        object.putInt("uid",seat.player.uid);
        send("see",object,room.getUserList());
    }

    private void sendHand(ZjhSeat seat){
        ISFSObject object = new SFSObject();
        object.putIntArray("hand",seat.hand);
        object.putInt("type",seat.type);
        send("sh",object,seat.player.user);
    }

    /**
     * 比牌
     * @param p 比牌的玩家
     * @param uid 被比牌的人的uid
     */
    public synchronized void vsRequest(Player p,int uid) {
        ZjhSeat from = (ZjhSeat) table.getSeat(p);
        ZjhSeat to = (ZjhSeat) table.getSeat(table.getPlayer(uid));
        if (from != null && from.action == 1 && table.playSeat.contains(to) && to.action != 4 && to.action != 5){
            if (table.turn > 3 ){
                if (from.type == table.SPECIAL && to.type == table.THREE_KIND){
                    to.action = 5;
                    from.action = 6;
                    notifyVSInfo(from,from,to);
                }else if (from.type == table.THREE_KIND && to.type == table.SPECIAL){
                    from.action = 5;
                    to.action = 6;
                    notifyVSInfo(from,to,from);
                }else if (from.type > to.type){
                    from.action = 6;
                    to.action = 5;
                    notifyVSInfo(from,from,to);
                }else if (from.type < to.type){
                    to.action = 6;
                    from.action = 5;
                    notifyVSInfo(from,to,from);
                }else {
                    boolean sameTiles = true;
                    for (int i = 0; i < from.arr.length; i++) {
                        if (from.arr[i] > to.arr[i]) {
                            to.action = 5;
                            from.action = 6;
                            notifyVSInfo(from,from,to);
                            sameTiles = false;
                            break;
                        } else if (from.arr[i] <to.arr[i]) {
                            to.action = 6;
                            from.action = 5;
                            notifyVSInfo(from,to,from);
                            sameTiles = false;
                            break;
                        }
                    }
                    if (sameTiles) {
                        from.action = 5;
                        to.action = 6;
                        notifyVSInfo(from,to,from);
                    }
                }
                if (from.action == 5 || from.action == 6){
                    from.sp.add(to);
                    to.sp.add(from);

                    cancelScheduledFuture(actionScheduled);

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (from.action == 5){
                        table.current = table.getNextActionSeat(table.current);
                        userAction(table.current);
                    }else {
                        if (table.current == table.getNextActionSeat(table.current)){
                            table.winner = table.current;
                            showHand();
                        }else{
                            table.current = table.getNextActionSeat(table.current);
                            userAction(table.current);
                        }
                    }
                }
            }
        }
    }

    private void notifyVSInfo(ZjhSeat from, ZjhSeat win, ZjhSeat lose) {
        ISFSObject object = new SFSObject();
        object.putInt("uid",from.player.uid);
        object.putInt("wid", win.player.uid);
        object.putInt("lid", lose.player.uid);
        send("vs", object, room.getUserList());
    }

    public void handRequest(Player p,int uid) {
        ZjhSeat seat = (ZjhSeat) table.getSeat(p);
        ZjhSeat toSee = (ZjhSeat) table.getSeat(table.getPlayer(uid));
        if (seat != null && toSee != null && toSee.hand.size() == 3 && p.vip){
            ISFSObject object = new SFSObject();
            object.putInt("uid",uid);
            object.putIntArray("hand",toSee.hand);
            send("v",object,p.user);
        }
    }

    @Override
    public synchronized boolean leaveRequest(Player p) {
        boolean leaved = super.leaveRequest(p);
        if (leaved){
            if (table.roundStart) {
                table.initStartGame();
                ISFSObject object = new SFSObject();
                object.putInt("c", table.currentCount);
                send("GStart", object, room.getUserList());

                addTableFloor();
            }
        }
        return leaved;
    }
}
