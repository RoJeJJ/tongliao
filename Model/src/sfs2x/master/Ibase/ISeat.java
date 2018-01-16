package sfs2x.master.Ibase;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import sfs2x.Constant;
import sfs2x.master.Player;

public abstract class ISeat {
    /**
     * 座位号
     */
    public final int no;
    /**
     * 是否是空位置
     */
    public boolean empty;
    /**
     * 该位置的玩家
     */
    public Player player;
    /**
     * 准备状态
     */
    public boolean ready;
    /**
     * 离线
     */
    public boolean offline;
    /**
     * 请求解散处理状态 0 未选择,1 同意解散,2,不同意解散
     */
    public int disbandCode;
    /**
     * 房卡房 分数
     */
    public long score;
    /**
     * 下一个玩家
     */
    public ISeat next;
    /**
     * 当局输赢
     */
    public int lw;
    /**
     * 总输赢
     */
    public int tlw;
    /**
     * 构造方法
     * @param no 座位号
     */
    public ISeat(int no){
        this.no = no;
        this.empty = true;
        this.player = null;
        this.score = 0;
        disbandCode = 0;
    }
    public abstract void initStartGame();
    public ISFSObject toSFSObject(){
        ISFSObject object = player.toSFSObject();
        object.putInt("no",no);
        object.putBool("ready",ready);
        object.putBool("off",offline);
        return object;
    }

    public void userLeave() {
        if (this.player != null)
            this.player.room = null;
        this.player = null;
        this.ready = false;
        this.score = 0;
        this.offline = false;
        this.empty = true;
        disbandCode = 0;
        lw = 0;
        next = null;
        tlw = 0;
    }

}
