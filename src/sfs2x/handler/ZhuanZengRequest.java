package sfs2x.handler;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import sfs2x.master.Player;
import sfs2x.utils.DBUtil;
import sfs2x.utils.Utils;

import java.sql.*;

public class ZhuanZengRequest extends BaseClientRequestHandler{
    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        Player p = Utils.getPlayer(user);
        int zid = isfsObject.getInt("uid");
        int card = isfsObject.getInt("card");
        ISFSObject object = new SFSObject();
        object.putInt("err",zhuanzeng(p.uid,zid,card));
        send("zz",object,user);
    }

    private int zhuanzeng(int uid,int zid,int card){
        Connection connection = DBUtil.getConnection();
        CallableStatement stmt = null;
        ResultSet set = null;
        try {
            if (connection != null){
                stmt = connection.prepareCall("{?=call zhuanzeng(?,?,?)}");
                stmt.registerOutParameter(1, Types.INTEGER);
                stmt.setInt(2,uid);
                stmt.setInt(3,zid);
                stmt.setInt(4,card);
                if (stmt.execute()){
                    set = stmt.getResultSet();
                    if (set.next()){
                        ISFSObject object = new SFSObject();
                        object.putInt("uid",uid);
                        object.putLong("card",set.getLong("uCard"));
                        getParentExtension().handleInternalMessage("card",object);
                        object = new SFSObject();
                        object.putInt("uid",zid);
                        object.putLong("card",set.getLong("zCard"));
                        getParentExtension().handleInternalMessage("card",object);
                    }
                }
                return stmt.getInt(1);
            }
            return -1;
        }catch (SQLException e){
            e.printStackTrace();
            return -1;
        }finally {
            DBUtil.close(connection,stmt,set);
        }
    }
}
