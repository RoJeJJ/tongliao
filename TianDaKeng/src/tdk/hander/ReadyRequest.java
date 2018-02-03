package tdk.hander;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import org.hsqldb.Table;
import sfs2x.Constant;
import sfs2x.master.Player;
import sfs2x.master.tdk.TdkSeat;
import sfs2x.master.tdk.TdkTable;
import sfs2x.utils.DBUtil;
import sfs2x.utils.Utils;
import tdk.TdkExtension;

import java.sql.Connection;

public class ReadyRequest extends BaseClientRequestHandler {
    @SuppressWarnings("unchecked")
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        Player p = Utils.getPlayer(user);
        TdkExtension<TdkSeat, TdkTable<TdkSeat>> ext = (TdkExtension) getParentExtension();
        Room room = ext.getParentRoom();
        boolean start = false;
        TdkSeat seat = ext.getTable().getSeat(p);
        if (seat != null && !seat.ready) {
            seat.ready = true;

            ISFSObject object = new SFSObject();
            object.putInt("uid", p.uid);
            send("re", object, room.getUserList());

            TdkExtension.GAME_START_LOCK.lock();
            if (ext.getTable().gameStart)
                TdkExtension.GAME_START_LOCK.unlock();
            TdkExtension.ROUND_START_LOCK.writeLock().lock();
            if (!ext.getTable().roundStart && ext.getTable().readyToStart()) {
                if (ext.getTable().cardRoom ){
                    if (!ext.getTable().takeOff){
                        ext.costCard();
                        ext.getTable().takeOff = true;
                    }
                }
                ext.getTable().roundStart = true;
                if (!ext.getTable().gameStart) {
                    ext.getTable().gameStart = true;
                    TdkExtension.GAME_START_LOCK.unlock();
                }
                start = true;
            }
            if (TdkExtension.GAME_START_LOCK.isLocked())
                TdkExtension.GAME_START_LOCK.unlock();
            TdkExtension.ROUND_START_LOCK.writeLock().unlock();
        }
        if (start) {
            ext.startGame();
        }
    }
}
