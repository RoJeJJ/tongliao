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
import sfs2x.utils.DBUtil;
import sfs2x.utils.Utils;

import java.util.List;

public class OnDisconnectedHandler extends BaseServerEventHandler{
    @Override
    public void handleServerEvent(ISFSEvent isfsEvent) throws SFSException {
        User user = (User) isfsEvent.getParameter(SFSEventParam.USER);
        Player player = Utils.getPlayer(user);
        user.getSession().removeProperty("t");
        ((ZoneExt)getParentExtension()).olp.remove(player.uid);
        DBUtil.signOut(player.uid);
        List rooms = (List) isfsEvent.getParameter(SFSEventParam.JOINED_ROOMS);
        if (rooms.size() > 0){
            Room room = (Room) rooms.get(0);
            if (room != null && room.isActive()) {//在房间中
                room.getExtension().handleInternalMessage("off",player);
            }
        }
    }
}
