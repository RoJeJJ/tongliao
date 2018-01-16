package sfs2x.handler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import sfs2x.Constant;
import sfs2x.ZoneExt;
import sfs2x.master.Player;
import sfs2x.utils.Utils;

public class OnDisconnectedHandler extends BaseServerEventHandler{
    @Override
    public void handleServerEvent(ISFSEvent isfsEvent) throws SFSException {
        User user = (User) isfsEvent.getParameter(SFSEventParam.USER);
        Player player = Utils.getPlayer(user);
        user.getSession().removeProperty("t");
        ((ZoneExt)getParentExtension()).olp.remove(player.uid);
        if (player.room != null && player.room.isActive()) {//在房间中
            player.room.getExtension().handleInternalMessage("off",player);
        }
    }
}
