package sfs2x.handler;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.v2.util.TaskScheduler;
import sfs2x.Constant;

public class OnRoomRemovedHandler extends BaseServerEventHandler{
    @Override
    public void handleServerEvent(ISFSEvent isfsEvent) throws SFSException {
        Room room = (Room) isfsEvent.getParameter(SFSEventParam.ROOM);
        int roomName = Integer.parseInt(room.getName());
        TaskScheduler taskScheduler = SmartFoxServer.getInstance().getTaskScheduler();
        taskScheduler.resizeThreadPool(taskScheduler.getThreadPoolSize() - 1 <= 3 ? 3 : taskScheduler.getThreadPoolSize() - 1);

        //回收房间名字,到房间名字数组
        synchronized (Constant.ROOM_NAME_LOCK){
            if (!Constant.roomName.contains(roomName))
                Constant.roomName.add(roomName);
        }
    }
}
