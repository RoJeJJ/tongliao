package sfs2x.handler;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import sfs2x.master.Player;
import sfs2x.utils.Utils;

public class RequestJoinRoomHandler extends BaseClientRequestHandler {
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        Player player = Utils.getPlayer(user);
        String roomName = isfsObject.getUtfString("name");
        Room room = getParentExtension().getParentZone().getRoomByName(roomName);
        ISFSObject object = new SFSObject();
        if (room == null || !room.isActive()){
            object.putInt("err",1);//房间不存在
            send("jr",object,user);
        }else {
            room.getExtension().handleInternalMessage("jr",player);
        }
    }
}
