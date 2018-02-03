package tdk.hander;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import sfs2x.master.Player;
import sfs2x.master.tdk.TdkSeat;
import sfs2x.master.tdk.TdkTable;
import sfs2x.utils.Utils;
import tdk.TdkExtension;

import java.util.ArrayList;
import java.util.Collections;

public class ActionRequest extends BaseClientRequestHandler {
    @SuppressWarnings("unchecked")
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        Player p = Utils.getPlayer(user);
        TdkExtension<TdkSeat,TdkTable<TdkSeat>> ext = (TdkExtension) getParentExtension();
        Room room = ext.getParentRoom();
        Integer ac = isfsObject.getInt("ac");
        TdkSeat seat = ext.getTable().getSeat(p);
        if (ext.getTable().current == seat && seat.action == 1) {
            if (seat.actioning)
                return;
            seat.actioning = true;
            if (ac == 0) {
//                seat.action = 4;
//            else if (ac == -1 && ext.getTable().turn > 5 && ext.getTable().lastActionBets == 0){
//                seat.action = -1;
//            }
                if (ext.getTable().turn > 5 && ext.getTable().lastActionBets == 0){ //大于5轮,选择不踢
                    trace("第"+ext.getTable().turn+"轮");
                    seat.action = -1;
                    ext.getTable().buTi.add(seat);
                }else
                    seat.action = 4;
            }else {
                if (ext.getTable().lastActionBets == 0 && ac > 0) {
                    seat.action = 2;
                } else if (ac == ext.getTable().lastActionBets)
                    seat.action = 2;
            }
            if (seat.action == 4 || seat.action == 2 || seat.action == -1) {
                if (seat.action == 2) {
                    seat.bets.add(ac);
                    ext.getTable().pot.add(ac);
                    ext.getTable().actionOrder.add(seat);

                    //更新下注
                    ext.getTable().lastAction = seat;
                    ext.getTable().lastActionBets = ac;
                }


                seat.actioning = false;

                //发送客户端
                ISFSObject object = new SFSObject();
                object.putInt("uid", seat.player.uid);
                object.putInt("call", ac);
                object.putIntArray("pot", ext.getTable().pot);
                object.putIntArray("bets", seat.bets);
                send("call", object, room.getUserList());

                if (ext.getTable().allAction()) { //全部下完注了
                    ext.getTable().lastAction = null;
                    ext.getTable().lastActionBets = 0;
                    if (ext.getTable().turn == 5 || ext.getTable().turn == 6) { //第5轮,第6轮 结束
                        if (ext.getTable().actionOrder.size() > 1){ //下注玩家在2个以上 选择踢 反踢

                            ext.getTable().tiOrder.clear();
                            for (int i=ext.getTable().actionOrder.size() - 1;i>=0;i--)
                                ext.getTable().tiOrder.add(ext.getTable().actionOrder.get(i));

                            ext.getTable().startNewTurn();
                            ext.getTable().actionOrder.clear();
                            ext.getTable().buTi.clear();

                            ext.getTable().tiIndex = 0;
                            ext.getTable().current = ext.getTable().tiOrder.get(ext.getTable().tiIndex);
                            ext.getTable().current.action = 1;
                            ext.userAction(ext.getTable().current);
                    } }else if (ext.getTable().actionOrder.size() == 1){ //只剩一个玩家了,该玩家直接胜利
                        ext.getTable().winner = ext.getTable().actionOrder.get(0);
                        ext.showHand();
                    }else if (ext.getTable().turn == 7) { //第7轮结束,开始比牌
                        ext.getTable().winner = ext.getTable().getWinner();
                        ext.showHand();
                    } else {
                        ext.getTable().startNewTurn();
                       ext. deal();
                        ext.getTable().current = ext.getTable().getMaxFace();
                        ext.getTable().firstAction = ext.getTable().current;
                        ext.userAction(ext.getTable().current);
                    }
                }else {
                    if (ext.getTable().turn > 5){
                        if (ext.getTable().noBody_Ti()){
                            ext.getTable().winner = ext.getTable().getWinner();
                            ext.showHand();
                        }else {
                            if (++ ext.getTable().tiIndex < ext.getTable().tiOrder.size())
                                ext.getTable().current = ext.getTable().tiOrder.get(ext.getTable().tiIndex);
                            else
                                ext.getTable().current = ext.getTable().tiOrder.get(ext.getTable().tiIndex - ext.getTable().tiOrder.size());
                            ext.userAction(ext.getTable().current);
                        }
                    }else {
                        if (seat.action == 4 && ext.getTable().firstAction == seat) {
                            ext.getTable().current = ext.getTable().getMaxFace();
                            ext.getTable().firstAction = ext.getTable().current;
                            ext.userAction(ext.getTable().current);
                        } else {
                            ext.getTable().current = ext.getTable().getNextAvailable(seat);
                            ext.userAction(ext.getTable().current);
                        }
                    }
                }
            }
        }
    }
}
