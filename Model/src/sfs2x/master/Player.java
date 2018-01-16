package sfs2x.master;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.protocol.serialization.SerializableSFSType;

public class Player implements SerializableSFSType{
    /**
     * 用户ID
     */
    public int uid;
    /**
     * 昵称
     */
    public String nick;
    /**
     * 性别
     */
    public int gender;
    /**
     * 头像
     */
    public String faceurl;
    /**
     * 房卡
     */
    public long card;//房卡
    /**
     * 绑定的user
     */
    public transient User user;
    /**
     * ip
     */
    public String ip;
    /**
     * 加入的房间
     */
    public Room room;
    /**
     * 代理ID
     */
    public int pid;
    /**
     *
     */
    public boolean vip;


    public Player(int uid,String nickname, int sex, String faceurl, long card,int pid,String ip,boolean vip) {
        this.uid = uid;
        this.ip = ip;
        this.nick = nickname;
        this.gender = sex;
        this.faceurl = faceurl;
        this.card = card;
        this.pid = pid;
        this.vip = vip;
    }
    public ISFSObject toSFSObject(){
        ISFSObject object = new SFSObject();
        object.putInt("uid",uid);
        object.putUtfString("nick",nick);
        object.putInt("sex",gender);
        object.putUtfString("face",faceurl);
        object.putLong("card",card);
        object.putUtfString("ip",ip);
        object.putInt("pid",pid);
        return object;
    }
}
