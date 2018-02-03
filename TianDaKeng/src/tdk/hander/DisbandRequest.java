package tdk.hander;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import sfs2x.master.Player;
import sfs2x.master.tdk.TdkSeat;
import sfs2x.master.tdk.TdkTable;
import sfs2x.utils.Utils;
import tdk.TdkExtension;

public class DisbandRequest extends BaseClientRequestHandler{
    @SuppressWarnings("unchecked")
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        Player p = Utils.getPlayer(user);
        TdkExtension<TdkSeat, TdkTable<TdkSeat>> ext = (TdkExtension) getParentExtension();
        Boolean e = isfsObject.getBool("e");
        if (e == null)
            return;
        TdkExtension.GAME_START_LOCK.lock();
        TdkSeat seat = ext.getTable().getSeat(p);
        if (seat == null || ext.getTable().applicant == null)
            return;
        if (seat.disbandCode == 0){
            if (e)
                seat.disbandCode = 1;
            else
                seat.disbandCode = 2;
            ext.sendDisbandInfo(null);
            ext.checkBreak();
        }
        TdkExtension.GAME_START_LOCK.unlock();
    }
}
