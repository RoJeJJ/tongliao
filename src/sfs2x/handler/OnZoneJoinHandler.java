package sfs2x.handler;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import sfs2x.Constant;
import sfs2x.ZoneExt;
import sfs2x.master.Player;
import sfs2x.utils.Utils;

public class OnZoneJoinHandler extends BaseServerEventHandler{
    @Override
    public void handleServerEvent(ISFSEvent isfsEvent) throws SFSException {
        User user = (User) isfsEvent.getParameter(SFSEventParam.USER);
        user.setPrivilegeId((short) 2);
        Player player = Utils.getPlayer(user);
        Room room = Constant.offlinePlayer.get(player.uid);
        if (room != null && room.isActive()){
            Player p = (Player) room.getExtension().handleInternalMessage("p",player.uid);
            if (p != null) {
                player = p;
                Utils.bindPlayer(user.getSession(),p);
            }
        }else
            Constant.offlinePlayer.remove(player.uid);
        player.user = user;
        ((ZoneExt)getParentExtension()).olp.putIfAbsent(player.uid,player);
        send("u",player.toSFSObject(),user);

        trace(player.vip);
        if (player.pid == 0){
            send("sa",null,user);
        }
        if (player.vip) {
            ISFSObject object = new SFSObject();
            object.putBool("vip",true);
            send("vv", object, user);
        }

        if ( room!= null && room.isActive()){
            room.getExtension().handleInternalMessage("join",player);
        }
    }

}
