package sfs2x;

import com.smartfoxserver.v2.entities.Room;

import java.util.ArrayList;
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
    public static final String JOIN_GROUP_LOCK = "join_group_lock";


    //开始游戏的等待时间
    public static final int WAIT_START = 15;
    //玩家下注等待时间
    public static final int BET_TIME = 20;

    public static final int DEFAULT_SCORE = 3000;

    public static final long DELAY_TIME_VS = 5000;
    public static final long DELAY_TIME_SELTT = 3000;

}
