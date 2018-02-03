package sfs2x.handler;

import com.smartfoxserver.v2.annotations.Instantiation;
import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.SFSRoomRemoveMode;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import org.apache.commons.lang.math.RandomUtils;
import sfs2x.Constant;
import sfs2x.master.Player;
import sfs2x.master.tdk.TdkTable;
import sfs2x.master.zjh.ZjhTable;
import sfs2x.utils.DBUtil;
import sfs2x.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Instantiation(Instantiation.InstantiationMode.SINGLE_INSTANCE)
public class CreateRoomHandler extends BaseClientRequestHandler {
    @Override
    public synchronized void handleClientRequest(User user, ISFSObject isfsObject) {
        Integer mod = isfsObject.getInt("mod");
//        Boolean classical = isfsObject.getBool("cl");//经典玩法
//        Integer floor = isfsObject.getInt("fl");//底分
//        Boolean hw = isfsObject.getBool("hw");
//        Boolean tg = isfsObject.getBool("tg");
        Boolean dk = isfsObject.getBool("dk");
        ISFSObject object = new SFSObject();
        Player player = Utils.getPlayer(user);
        if (mod != null){
            switch (mod){
                case 0: //扎金花
                    Boolean aa = isfsObject.getBool("aa");//
                    Integer count = isfsObject.getInt("co");//局数
                    Integer men = isfsObject.getInt("me");//必闷
                    Boolean sf = isfsObject.getBool("sf");//
                    //参数检查
                    if (aa == null || count == null || men == null ||
                            sf == null || dk == null) {
                        object.putInt("err", 1);//参数错误
                        send("cr", object, user);
                    }else if ((count != 8 && count != 16)) {
                        object.putInt("err", 2);//局数设置错误
                        send("cr", object, user);
                    } else if (men != 0 && men != 1 && men != 2 && men != 3) {
                        object.putInt("err", 3);//闷设置错误
                        send("cr", object, user);
                    }else {
                        //房卡检查
                    int need = Utils.need_zjh(aa, count);
                    int roomName;
                    if (DBUtil.lockCard(player.uid, need)) {
                        CreateRoomSettings roomSettings = new CreateRoomSettings();
                        synchronized (Constant.ROOM_NAME_LOCK) {
                            int index = RandomUtils.nextInt(Constant.roomName.size());
                            roomName = Constant.roomName.remove(index);
                            roomSettings.setName(String.valueOf(roomName));
                        }
                        roomSettings.setAutoRemoveMode(SFSRoomRemoveMode.NEVER_REMOVE);
                        roomSettings.setDynamic(true);
                        roomSettings.setGame(true);
                        roomSettings.setMaxUsers(6);
                        roomSettings.setMaxSpectators(0);
                        ZjhTable table = new ZjhTable(0, aa, player.uid, 6,count, men, sf, need);
                        Map<Object, Object> properties = new HashMap<>();
                        properties.put("t", table);
                        roomSettings.setRoomProperties(properties);
                        CreateRoomSettings.RoomExtensionSettings extensionSettings =
                                new CreateRoomSettings.RoomExtensionSettings("zjhExt", "zjh.ZjhExtension");
                        roomSettings.setExtension(extensionSettings);
                        try {
                            Room room = getApi().createRoom(getParentExtension().getParentZone(), roomSettings, null);
                            object.putInt("err", 0);//创建成功
                            send("cr", object, user);
                            if (!dk)
                                room.getExtension().handleInternalMessage("jr",player);
                        } catch (SFSCreateRoomException e) {
                            DBUtil.unLockCard(player.uid, need); //解锁房卡
                            synchronized (Constant.ROOM_NAME_LOCK) {
                                if (!Constant.roomName.contains(roomName))
                                    Constant.roomName.add(roomName);
                            }
                            object.putInt("err", 4);//创建失败
                            send("cr", object, user);
                        }
                    } else {
                        object.putInt("err", -1);//房卡不足
                        send("cr", object, user);
                    }
                }
                    break;
                case 1://填大坑
                    Integer c = isfsObject.getInt("co");
                    boolean lg = isfsObject.getBool("lg");
                    int err;
                    Room room = null;
                    System.out.println(c);
                    if (c == null || !Constant.TDK_CARD_COUNT.containsKey(c))
                        err = 1;
                    else {
                        int need = Constant.TDK_CARD_COUNT.get(c);
                        int roomName;
                        if (DBUtil.lockCard(player.uid,need)){
                            CreateRoomSettings roomSettings = new CreateRoomSettings();
                            synchronized (Constant.ROOM_NAME_LOCK) {
                                int index = new Random().nextInt(Constant.roomName.size());
                                roomName = Constant.roomName.remove(index);
                                roomSettings.setName(String.valueOf(roomName));
                                roomSettings.setAutoRemoveMode(SFSRoomRemoveMode.NEVER_REMOVE);
                                roomSettings.setDynamic(true);
                                roomSettings.setGame(true);
                                roomSettings.setMaxUsers(5);
                                roomSettings.setMaxSpectators(0);
                                Map<Object,Object> properties = new HashMap<>();
                                TdkTable table = new TdkTable(1,false,true,player.uid,5,c,need);
                                properties.put("t",table);
                                roomSettings.setRoomProperties(properties);
                                CreateRoomSettings.RoomExtensionSettings extensionSettings = new CreateRoomSettings.RoomExtensionSettings("tdk","tdk.TdkExtension");
                                roomSettings.setExtension(extensionSettings);
                                try {
                                    room = getApi().createRoom(getParentExtension().getParentZone(),roomSettings,null);
                                    err = 0;
                                } catch (SFSCreateRoomException e) {
                                    e.printStackTrace();
                                    DBUtil.unLockCard(player.uid,need);
                                    synchronized (Constant.ROOM_NAME_LOCK) {
                                        if (!Constant.roomName.contains(roomName))
                                            Constant.roomName.add(roomName);
                                    }
                                    err = 4;
                                }
                            }
                        }else
                            err = -1;
                    }
                    ISFSObject o = new SFSObject();
                    o.putInt("err",err);
                    send("jr",o,user);
                    if (err == 0 && room != null && !dk)
                        room.getExtension().handleInternalMessage("jr",player);
                    break;
            }
        }
    }
}
