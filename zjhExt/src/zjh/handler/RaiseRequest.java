package zjh.handler;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import sfs2x.master.Player;
import sfs2x.master.zjh.ZjhSeat;
import sfs2x.utils.Utils;
import zjh.ZjhExtension;

public class RaiseRequest extends BaseClientRequestHandler{
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        Player p = Utils.getPlayer(user);
        ZjhExtension ext = (ZjhExtension) getParentExtension();

        Integer raise = isfsObject.getInt("c");
        if (raise != null )
            ext.raiseRequest(p,raise);
    }
}
