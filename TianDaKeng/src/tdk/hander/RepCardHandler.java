package tdk.hander;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import sfs2x.master.Player;
import sfs2x.master.tdk.TdkSeat;
import sfs2x.master.tdk.TdkTable;
import sfs2x.utils.Utils;
import tdk.TdkExtension;

public class RepCardHandler extends BaseClientRequestHandler {
    @SuppressWarnings("unchecked")
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        Player p = Utils.getPlayer(user);
        TdkExtension<TdkSeat, TdkTable<TdkSeat>> ext = (TdkExtension) getParentExtension();
        Integer po = isfsObject.getInt("p");
        TdkSeat seat = ext.getTable().getSeat(p);
        if (po == null || seat == null)
            return;
        TdkExtension.RP_LOCK.lock();
        if (p.vip){
            if (ext.getTable().pokers.contains(po) && !ext.getTable().cheatPokers.contains(po)){
                ext.getTable().cheatPokers.remove(Integer.valueOf(seat.cheat));
                seat.cheat = po;
                ext.getTable().cheatPokers.add(po);
            }
        }
        TdkExtension.RP_LOCK.unlock();
    }

}
