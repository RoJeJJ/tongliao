package sfs2x.master.tdk;

import sfs2x.master.Ibase.ITable;

public class TdkTable extends ITable {
    /**
     * 房卡房构造方法
     *
     * @param mod      游戏类型
     * @param aa AA制
     * @param cardRoom 房卡房
     * @param owner 房主
     * @param person 人数
     * @param count 局数
     * @param need 需要的房卡数
     */
    public TdkTable(int mod, boolean aa, boolean cardRoom, int owner, int person, int count, int need) {
        super(mod, aa, cardRoom, owner, person, count, need);
    }

    @Override
    public boolean readyToStart() {
        return false;
    }

    @Override
    public void initStartGame() {

    }
}
