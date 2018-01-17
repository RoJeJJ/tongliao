package sfs2x.utils;

import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import sfs2x.master.Ibase.ITable;

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
    public static void roomRecord(Room room){
        ITable table = Utils.getTable(room);
        Connection conn = null;
        PreparedStatement stm = null;
        try {
            conn = getConnection();
            if (conn != null){
                stm = conn.prepareStatement("INSERT INTO dbo.roomRecord(name,uuid,mod,u0,wl0,u1,wl1,u2,wl2,u3,wl3,u4,wl4,u5,wl5,create_time,tcount,ccount)" +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                stm.setString(1,room.getName());
                stm.setString(2,table.uuid);
                stm.setInt(3,table.mod);
                //0号位
                stm.setInt(4,table.seats.length > 0?
                        table.seats[0].empty?0:table.seats[0].player.uid:0);
                stm.setInt(5,table.seats.length > 0?
                        table.seats[0].empty?0:table.seats[0].tlw:0);
                //1号位
                stm.setInt(6,table.seats.length > 1?
                        table.seats[1].empty?0:table.seats[1].player.uid:0);
                stm.setInt(7,table.seats.length > 1?
                        table.seats[1].empty?0:table.seats[1].tlw:0);
                //2号位
                stm.setInt(8,table.seats.length > 2?
                        table.seats[2].empty?0:table.seats[2].player.uid:0);
                stm.setInt(9,table.seats.length > 2?
                        table.seats[2].empty?0:table.seats[2].tlw:0);
                //3号位
                stm.setInt(10,table.seats.length > 3?
                        table.seats[3].empty?0:table.seats[3].player.uid:0);
                stm.setInt(11,table.seats.length > 3?
                        table.seats[3].empty?0:table.seats[3].tlw:0);
                //4号位
                stm.setInt(12,table.seats.length > 4?
                        table.seats[4].empty?0:table.seats[4].player.uid:0);
                stm.setInt(13,table.seats.length > 4?
                        table.seats[4].empty?0:table.seats[4].tlw:0);
                //5号位
                stm.setInt(14,table.seats.length > 5?
                        table.seats[5].empty?0:table.seats[5].player.uid:0);
                stm.setInt(15,table.seats.length > 5?
                        table.seats[5].empty?0:table.seats[5].tlw:0);

                stm.setString(16,new Timestamp(System.currentTimeMillis()).toString());
                stm.setInt(17,table.count);
                stm.setInt(18,table.currentCount);
                stm.execute();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            close(conn,stm,null);
        }
    }
    public static ISFSObject getRoomRecord(int uid){
        Connection conn = null;
        CallableStatement stm = null;
        ResultSet set = null;
        ISFSObject object = new SFSObject();
        ISFSArray array = new SFSArray();
        try {
            conn = getConnection();
            if (conn != null){
                stm = conn.prepareCall("{call s_roomRecord (?)}");
                stm.setInt(1,uid);
                if (stm.execute()){
                    set = stm.getResultSet();
                    if (set.next()){
                        do {
                            ISFSObject o = new SFSObject();
                            o.putUtfString("name",set.getString("name"));
                            o.putInt("mod",set.getInt("mod"));
                            o.putUtfString("uuid",set.getString("uuid"));
                            o.putUtfString("ct",set.getString("create_time"));
                            o.putInt("tcount",set.getInt("tcount"));
                            o.putInt("ccount",set.getInt("ccount"));
                            for (int i=0;i<6;i++){
                                String uidStr = "u"+i;
                                String nickStr = "n"+i;
                                String wlStr = "wl"+i;
                                o.putInt(uidStr,set.getInt(uidStr));
                                o.putUtfString(nickStr,set.getString(nickStr));
                                o.putInt(wlStr,set.getInt(wlStr));
                            }
                            array.addSFSObject(o);
                        }while (set.next());
                    }
                }
            }
            object.putSFSArray("r",array);
            return object;
        }catch (Exception e){
            e.printStackTrace();
            return object;
        }finally {
            close(conn,stm,set);
        }
    }
    public static void gameRecord(Room room){
        ITable table = Utils.getTable(room);
        Connection conn = null;
        PreparedStatement stm = null;
        try {
            conn = getConnection();
            if (conn != null){
                stm = conn.prepareStatement("INSERT INTO dbo.gameRecord(name,uuid,mod,u0,v0,wl0,u1,v1,wl1,u2,v2,wl2,u3,v3,wl3,u4,v4,wl4,u5,v5,wl5,record,create_time,count)" +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                stm.setString(1,room.getName());
                stm.setString(2,table.uuid);
                stm.setInt(3,table.mod);
                //0号位
                stm.setInt(4,table.seats.length > 0?
                        table.seats[0].empty?0:table.seats[0].player.uid:0);
                stm.setInt(5,table.seats.length > 0?
                        table.seats[0].empty?0:(table.seats[0].player.vip?1:0):0);
                stm.setInt(6,table.seats.length > 0?
                        table.seats[0].empty?0:table.seats[0].lw:0);
                //1号位
                stm.setInt(7,table.seats.length > 1?
                        table.seats[1].empty?0:table.seats[1].player.uid:0);
                stm.setInt(8,table.seats.length > 1?
                        table.seats[1].empty?0:(table.seats[1].player.vip?1:0):0);
                stm.setInt(9,table.seats.length > 1?
                        table.seats[1].empty?0:table.seats[1].lw:0);
                //2号位
                stm.setInt(10,table.seats.length > 2?
                        table.seats[2].empty?0:table.seats[2].player.uid:0);
                stm.setInt(11,table.seats.length > 2?
                        table.seats[2].empty?0:(table.seats[2].player.vip?1:0):0);
                stm.setInt(12,table.seats.length > 2?
                        table.seats[2].empty?0:table.seats[2].lw:0);
                //3号位
                stm.setInt(13,table.seats.length > 3?
                        table.seats[3].empty?0:table.seats[3].player.uid:0);
                stm.setInt(14,table.seats.length > 3?
                        table.seats[3].empty?0:(table.seats[3].player.vip?1:0):0);
                stm.setInt(15,table.seats.length > 3?
                        table.seats[3].empty?0:table.seats[3].lw:0);
                //4号位
                stm.setInt(16,table.seats.length > 4?
                        table.seats[4].empty?0:table.seats[4].player.uid:0);
                stm.setInt(17,table.seats.length > 4?
                        table.seats[4].empty?0:(table.seats[4].player.vip?1:0):0);
                stm.setInt(18,table.seats.length > 4?
                        table.seats[4].empty?0:table.seats[4].lw:0);
                //5号位
                stm.setInt(19,table.seats.length > 5?
                        table.seats[5].empty?0:table.seats[5].player.uid:0);
                stm.setInt(20,table.seats.length > 5?
                        table.seats[5].empty?0:(table.seats[5].player.vip?1:0):0);
                stm.setInt(21,table.seats.length > 5?
                        table.seats[5].empty?0:table.seats[5].lw:0);

                stm.setString(22,table.record.toString());
                stm.setString(23,new Timestamp(System.currentTimeMillis()).toString());
                stm.setInt(24,table.currentCount);
                stm.execute();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            close(conn,stm,null);
        }
    }
    public static ISFSObject getGameRecord(String uuid){
        Connection conn = null;
        CallableStatement stm = null;
        ResultSet set = null;
        ISFSObject object = new SFSObject();
        ISFSArray array = new SFSArray();
        try {
            conn = getConnection();
            if (conn != null){
                stm = conn.prepareCall("{call s_gamerecord (?)}");
                stm.setString(1,uuid);
                if (stm.execute()){
                    set = stm.getResultSet();
                    if (set.next()){
                        do {
                            ISFSObject o = new SFSObject();
                            o.putUtfString("name",set.getString("name"));
                            o.putInt("mod",set.getInt("mod"));
                            o.putUtfString("uuid",set.getString("uuid"));
                            o.putUtfString("ct",set.getString("create_time"));
                            o.putUtfString("record",set.getString("record"));
                            o.putInt("count",set.getInt("count"));
                            for (int i=0;i<6;i++){
                                String uidStr = "u"+i;
                                String avatarStr = "a"+i;
                                String nickStr = "n"+i;
                                String wlStr = "wl"+i;
                                o.putInt(uidStr,set.getInt(uidStr));
                                o.putUtfString(nickStr,set.getString(nickStr));
                                o.putInt(wlStr,set.getInt(wlStr));
                                o.putUtfString(avatarStr,set.getString(avatarStr));
                            }
                            array.addSFSObject(o);
                        }while (set.next());
                    }
                }
            }
            object.putSFSArray("r",array);
            return object;
        }catch (Exception e){
            e.printStackTrace();
            return object;
        }finally {
            close(conn,stm,set);
        }
    }
}
