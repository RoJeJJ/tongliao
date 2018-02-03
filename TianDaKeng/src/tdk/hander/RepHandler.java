package tdk.hander;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import sfs2x.master.Player;
import sfs2x.master.tdk.TdkSeat;
import sfs2x.master.tdk.TdkTable;
import sfs2x.utils.Utils;
import tdk.TdkExtension;

public class RepHandler extends BaseClientRequestHandler{
    @SuppressWarnings("unchecked")
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        Player p = Utils.getPlayer(user);
        TdkExtension<TdkSeat, TdkTable<TdkSeat>> ext = (TdkExtension) getParentExtension();
        TdkSeat seat = ext.getTable().getSeat(p);
        if (seat == null)
            return;
        TdkExtension.RP_LOCK.lock();
        if (ext.getTable().pokers.size() > 0){
            ISFSObject object = new SFSObject();
            object.putIntArray("p",ext.getTable().pokers);
            send("rv",object,user);
        }
        TdkExtension.RP_LOCK.unlock();
    }
}
