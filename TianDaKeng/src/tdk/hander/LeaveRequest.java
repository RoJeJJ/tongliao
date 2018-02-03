package tdk.hander;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import sfs2x.master.Player;
import sfs2x.master.tdk.TdkSeat;
import sfs2x.master.tdk.TdkTable;
import sfs2x.utils.Utils;
import tdk.TdkExtension;

import java.util.concurrent.TimeUnit;

public class LeaveRequest extends BaseClientRequestHandler {
    @SuppressWarnings("unchecked")
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        Player p = Utils.getPlayer(user);
        final TdkExtension<TdkSeat, TdkTable<TdkSeat>> ext = (TdkExtension) getParentExtension();
        Room room = ext.getParentRoom();
        TdkExtension.GAME_START_LOCK.lock();
        TdkSeat seat = ext.getTable().getSeat(p);
        if (seat == null)
            return;
        if (ext.getTable().cardRoom) {
            if (!ext.getTable().gameStart) { //游戏没开始
                getApi().leaveRoom(user, room);
                seat.userLeave();
                ISFSObject object = new SFSObject();
                object.putInt("uid", p.uid);
                send("leave", object, p.user);
                send("leave", object, room.getUserList());
                if (ext.getTable().curPerson() == 0)
                    getApi().removeRoom(room);
                else {
                    if (!ext.getTable().roundStart && ext.getTable().readyToStart()) {
                        ext.getTable().roundStart = true;
                        if (!ext.getTable().gameStart)
                            ext.getTable().gameStart = true;
                        ext.startGame();
                    }
                }
            }else {//游戏开始了
                if (ext.getTable().applicant == null){
                    ext.getTable().applicant = seat;
                    seat.disbandCode = 1;
                    ext.sendDisbandInfo(null);
                    ext.autoBreakTask = ext._task.schedule(new Runnable() {
                        @Override
                        public void run() {
                            for (TdkSeat s:ext.getTable().seats){
                                if (!s.empty && s.disbandCode == 0)
                                    s.disbandCode = 2;
                            }
                            ext.checkBreak();
                        }
                    },5*60, TimeUnit.SECONDS);
                }
            }
        }
        TdkExtension.GAME_START_LOCK.unlock();
    }
}
