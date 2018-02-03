package sfs2x.master.Ibase;


import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import sfs2x.master.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class ITable<T extends ISeat> {
    /**
     *唯一标识
     */
    public final String uuid;
    /**
     * 游戏类型
     */
    public final int mod ;
    /**
     * 总局数
     */
    public final int count;
    /**
     * 当前局数
     */
    public int currentCount;
    /**
     * 是否房卡房
     */
    public final boolean cardRoom;
    /**
     * 房卡房是否扣费
     */
    public boolean takeOff;
    /**
     * 房主
     */
    public int owner;
    /**
     *  是否AA制
     */
    public final boolean aa;
    /**
     * 房卡房游戏局数,默认值 -1;
     */
    public final int person;
    /**
     * 房卡房消耗的的房卡数
     */
    public final int need;
    /**
     * 房卡房牌局开始标志
     */
    public boolean gameStart;
    /**
     * 回合开始
     */
    public boolean roundStart;
    /**
     * 所有座位
     */
    public  T[] seats;
    /**
     * 当局游戏中的位置
     */
    public List<T> playSeat;
    public StringBuffer record;
    /**
     *  房卡房构造方法
     * @param mod 游戏类型
     */
    public ITable(int mod, boolean aa, boolean cardRoom, int owner, int person, int count, int need){
        this.mod = mod;
        this.aa = aa;
        this.person = person;
        this.need = need;
        this.cardRoom = cardRoom;
        this.owner = owner;
        this.count = count;
        playSeat = new ArrayList<>();
        takeOff = false;
        uuid = UUID.randomUUID().toString();
        currentCount = 0;
        record = new StringBuffer();
    }

    /**
     * 当前人数
     * @return 当前人数
     */
    public int curPerson(){
        int n = 0;
        for (T seat:seats){
            if (!seat.empty)
                n++;
        }
        return n;
    }


    /**
     * 获取玩家所在的座位
     * @param p 玩家
     * @return 座位
     */
    public T getSeat(Player p){
        for (T s:seats){
            if (!s.empty && s.player == p) {
                return s;
            }
        }
        return null;
    }

    /**
     * 是否可以开始游戏
     * @return true 可以开始,false 不可以
     */
    public abstract boolean readyToStart();
    public boolean isExists(Player p){
        for (T seat:seats){
            if (!seat.empty && seat.player == p)
                return true;
        }
        return false;
    }

    /**
     *
     * @return 房间
     */
    public ISFSObject toSFSObject(Player p){
        ISFSObject object = new SFSObject();
        object.putInt("mod",mod);
        object.putBool("cr",cardRoom);
        object.putInt("oid",owner);
        object.putBool("aa",aa);
        object.putInt("p",person);
        object.putInt("cp",curPerson());
        object.putBool("gs",gameStart);
        object.putBool("rs",roundStart);
        object.putInt("c",count);
        object.putInt("cc",currentCount);
        return object;
    }

    public void join(Player p){
        for (T s:seats){
            if (s.empty) {
                s.player = p;
                s.empty = false;
                return;
            }
        }
    }

    public abstract void initStartGame();
    public void initForNext(){
        roundStart = false;
    }

    public Player getPlayer(int uid){
        for (T s:seats){
            if (!s.empty && s.player.uid == uid)
                return s.player;
        }
        return null;
    }
}
