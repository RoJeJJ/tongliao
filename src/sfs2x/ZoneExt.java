package sfs2x;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;
import sfs2x.handler.*;
import sfs2x.master.Player;
import sfs2x.utils.DBUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZoneExt extends SFSExtension{
    public ConcurrentMap<Integer,Player> olp = new ConcurrentHashMap<>();
    @Override
    public void init() {
        initData();
        addEventHandler(SFSEventType.USER_LOGIN,OnLoginHandler.class);
        addEventHandler(SFSEventType.USER_JOIN_ZONE,OnZoneJoinHandler.class);
        addEventHandler(SFSEventType.ROOM_REMOVED,OnRoomRemovedHandler.class);
        addEventHandler(SFSEventType.USER_DISCONNECT,OnDisconnectedHandler.class);
        addEventHandler(SFSEventType.USER_LOGOUT,OnDisconnectedHandler.class);

        addRequestHandler("p",PingHandler.class);//接收ping
        addRequestHandler("cr",CreateRoomHandler.class);//创建房间
        addRequestHandler("jr",RequestJoinRoomHandler.class);//加入房间
        addRequestHandler("sa",SetAgentHandler.class);
        addRequestHandler("curRoom", UserCurRoomHandler.class);
        addRequestHandler("dmr", DismissRoomHandler.class);
        addRequestHandler("zz",ZhuanZengRequest.class);//转赠
        addRequestHandler("rr", RoomRecordHandler.class);

        DBUtil.initDB(getParentZone().getDBManager());
    }

    private void initData() {
        for (int i=100000;i<1000000;i++){
            Constant.roomName.add(i);
        }
    }

    @Override
    public Object handleInternalMessage(String cmdName, Object params) {
        switch (cmdName) {
            case "card": {
                ISFSObject object = (ISFSObject) params;
                long card = object.getLong("card");
                int uid = object.getInt("uid");
                Player p = olp.get(uid);
                if (p != null) {
                    p.card = card;
                    send("uc", object, p.user);
                }
                break;
            }
            case "halt":
                for (User user : getParentZone().getUserList())
                    getApi().logout(user);
                for (Room room : getParentZone().getRoomList()) {
                    getApi().removeRoom(room);
                }
                SmartFoxServer.getInstance().halt();
                break;
            case "vip": {
                ISFSObject object = (ISFSObject) params;
                int uid = object.getInt("uid");
                boolean vip = object.getBool("vip");
                Player p = olp.get(uid);
                if (p != null) {
                    p.vip = vip;
                    send("vv", object, p.user);
                }
                break;
            }
        }
        return null;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
