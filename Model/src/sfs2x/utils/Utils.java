package sfs2x.utils;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import sfs2x.Constant;
import sfs2x.master.Ibase.ITable;
import sfs2x.master.Player;

public class Utils {
    public static <T extends ITable>T getTable(Room room){
        Object object = room.getProperty("t");
        return  (T) object;
    }

    public static Player getPlayer(User user){
        return (Player) user.getSession().getProperty("p");
    }
    public static void bindPlayer(ISession session,Player p){
        session.setProperty("p",p);
    }


    public static int need_zjh(boolean aa,int count){
        if (!aa){
                if (count == 8)
                    return  1;
                else
                    return  2;
        }else {
            if (count == 8)
                return  1;
            else
                return  2;
        }
    }
}
