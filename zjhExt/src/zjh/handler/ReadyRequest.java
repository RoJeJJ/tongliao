package zjh.handler;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import sfs2x.master.Ibase.IExtension;
import sfs2x.master.Player;
import sfs2x.utils.Utils;

public class ReadyRequest extends BaseClientRequestHandler{
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        Player p = Utils.getPlayer(user);
        IExtension ext = (IExtension) getParentExtension();
        ext.readyRequest(p);
    }
}
