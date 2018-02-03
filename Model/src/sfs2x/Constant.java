package sfs2x;

import com.smartfoxserver.v2.entities.Room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constant {
    public static boolean debug = true;
    public static final String PASSWORD = "*ruidi19930723#";
    public static String USERINFO_URI = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
    public static final String TABLE = "table";
    public static final String PLAYER = "player";
    public static ConcurrentHashMap<Integer,Room> offlinePlayer = new ConcurrentHashMap<>();
    public static ArrayList<Integer> roomName = new ArrayList<>();

    //lock
    public static final String ROOM_NAME_LOCK = "room_name_lock";

    public static final Map<Integer,Integer> TDK_CARD_COUNT ;
    static {
        Map<Integer,Integer> map = new HashMap<>();
        map.put(8,1);
        map.put(16,2);
        map.put(40,3);
        TDK_CARD_COUNT = map;
    }

}
