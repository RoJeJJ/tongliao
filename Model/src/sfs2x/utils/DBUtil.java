package sfs2x.utils;

import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.entities.data.ISFSObject;

import java.sql.*;

public class DBUtil {
    private static IDBManager idbManager ;

    public static void initDB(IDBManager manager){
        idbManager = manager;
    }

    public static Connection getConnection(){
        Connection conn = null;
        try {
            conn =  idbManager.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void close(Connection conn,Statement statement,ResultSet set){
            try {
                if (conn != null)
                    conn.close();
                if (statement != null)
                    statement.close();
                if (set != null)
                    set.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }


    public static void signIn(int uid){
        Connection conn = null;
        CallableStatement stmt = null;
        try {
            conn = getConnection();
            if (conn != null){
                stmt = conn.prepareCall("{call RecordSignIn(?)}");
                stmt.setInt(1,uid);
                stmt.execute();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            close(conn,stmt,null);
        }
    }

    public static boolean lockCard(int uid,long card){
        Connection conn = null;
        CallableStatement stmt = null;
        try {
            conn = getConnection();
            if (conn != null){
                stmt = conn.prepareCall("{?=call lockCard(?,?)}");
                stmt.registerOutParameter(1,Types.INTEGER);
                stmt.setInt(2,uid);
                stmt.setLong(3,card);
               stmt.execute();
                return stmt.getInt(1) == 0;
            }
            return false;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }finally {
            close(conn,stmt,null);
        }
    }

    public static void unLockCard(int uid,int cost){
        Connection connection = null;
        CallableStatement stmt = null;
        try {
            connection = getConnection();
            if (connection != null) {
                stmt = connection.prepareCall("{call unlockCard (?,?)}");
                stmt.setInt(1,uid);
                stmt.setInt(2,cost);
                stmt.execute();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            close(connection,stmt,null);
        }
    }
    public static int systemStatus(){
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet set = null;
        try {
            conn = getConnection();
            if (conn != null){
                stmt = conn.prepareStatement("SELECT StatusValue FROM dbo.SystemStatusInfo WHERE StatusName='LoginEnable'");
                if (stmt.execute()){
                    set = stmt.getResultSet();
                    if (set.next())
                        return set.getInt("StatusValue");
                }
            }
            return 1;
        }catch (SQLException e){
            e.printStackTrace();
            return 1;
        }finally {
            close(conn,stmt,set);
        }
    }

    public static long getCard(int uid){
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet set = null;
        try {
            conn = getConnection();
            if (conn != null){
                stmt = conn.prepareStatement("SELECT card FROM user_info WHERE userid=?");
                stmt.setInt(1,uid);
                if (stmt.execute()){
                    set = stmt.getResultSet();
                    if (set.next())
                        return set.getLong("card");
                }
            }
            return -1;
        }catch (SQLException e){
            return -1;
        }finally {
            close(conn,stmt,set);
        }
    }

    public static int setAgent(int uid,int aid){
        Connection conn = null;
        CallableStatement stm = null;
        ResultSet set = null;
        try {
            conn = getConnection();
            if (conn != null){
                stm = conn.prepareCall("{call setAgent (?,?)}");
                stm.setInt(1,uid);
                stm.setInt(2,aid);
                if (stm.execute()){
                    set = stm.getResultSet();
                    if (set.next()) {
                        return set.getInt("pid");
                    }
                }
            }
            return 0;
        }catch (SQLException e){
            e.printStackTrace();
            return 0;
        }finally {
            close(conn,stm,set);
        }
    }
}
