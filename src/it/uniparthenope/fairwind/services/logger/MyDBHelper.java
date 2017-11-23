package it.uniparthenope.fairwind.services.logger;

import it.uniparthenope.fairwind.Log;

import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by raffaelemontella on 11/07/2017.
 */
public class MyDBHelper implements DBHelper {

    public static final String LOG_TAG="DBHELPER";

    public static final String DATABASE_NAME = "fairwind.db";
    public static final String FILES_TABLE_NAME = "toUpload";
    public static final String UPLOADING_TABLE_NAME = "uploading";
    public static final String FILES_COLUMN_PATH = "path";

    /*
    public static final String STATUS_COLUMN = "status";
    public static final String STATIC = "1";
    public static final String SENDING = "2";
    */

    private String url;

    public MyDBHelper(String path) {
        url="jdbc:sqlite:"+ path;

        try (Connection conn = connect()) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();

                String sql1 = "create table if not exists "+FILES_TABLE_NAME+" " +
                        "("+FILES_COLUMN_PATH+" text primary key)";

                String sql2 = "create table if not exists " + UPLOADING_TABLE_NAME + " (" + FILES_COLUMN_PATH + " text primary key)";

                Statement stmt = conn.createStatement();
                stmt.execute(sql1);
                stmt.execute(sql2);

                conn.close();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public Integer insertFile(String path) {
        String sql = "INSERT INTO "+FILES_TABLE_NAME+"("+FILES_COLUMN_PATH+") VALUES(\""+path+"\")";
        System.out.println("INSERTING: " + sql);
        Connection conn=null;
        try {
            conn = connect();
            if (conn!=null) {
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
            }

        } catch (SQLException ex) {
            Log.e(LOG_TAG,ex.getMessage());
        }

        if (conn!=null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Log.e(LOG_TAG,ex.getMessage());
            }
        }
        System.out.println(numberOfRows());
        return numberOfRows();
    }

    @Override
    public Integer insertFileInUploading(String path) {
        String sql = "INSERT INTO "+UPLOADING_TABLE_NAME+"("+FILES_COLUMN_PATH+") VALUES(\""+path+"\")";
        System.out.println("INSERTING: " + sql);
        Connection conn=null;
        try {
            conn = connect();
            if (conn!=null) {
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
            }

        } catch (SQLException ex) {
            Log.e(LOG_TAG,ex.getMessage());
        }

        if (conn!=null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Log.e(LOG_TAG,ex.getMessage());
            }
        }

        return numberOfUploadingRows();
    }

    @Override
    public Integer deleteFile(String path) {
        String sql = "DELETE FROM "+FILES_TABLE_NAME+" where "+FILES_COLUMN_PATH+"=\""+path+"\"";

        Connection conn=null;
        try {
            conn = connect();
            if (conn!=null) {
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
            }
        } catch (SQLException ex) {
            Log.e(LOG_TAG,ex.getMessage());
        }

        if (conn!=null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Log.e(LOG_TAG,ex.getMessage());
            }
        }
        return numberOfRows();
    }

    @Override
    public Integer deleteFileFromUploading(String path) {
        String sql = "DELETE FROM "+UPLOADING_TABLE_NAME+" where "+FILES_COLUMN_PATH+"=\""+path+"\"";

        Connection conn=null;
        try {
            conn = connect();
            if (conn!=null) {
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
            }
        } catch (SQLException ex) {
            Log.e(LOG_TAG,ex.getMessage());
        }

        if (conn!=null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Log.e(LOG_TAG,ex.getMessage());
            }
        }

        return numberOfUploadingRows();
    }

    @Override
    public ArrayList<String> getAllFiles() {

        int n=numberOfRows();

        ArrayList<String> result=new ArrayList<String>();

        String sql="SELECT "+FILES_COLUMN_PATH+" FROM "+FILES_TABLE_NAME;

        Connection conn=null;

        try {
            conn = this.connect();
            if (conn!=null) {
                Statement stmt  = conn.createStatement();
                ResultSet rs    = stmt.executeQuery(sql);

                // loop through the result set
                while (rs.next()) {
                    result.add(rs.getString(FILES_COLUMN_PATH));
                }

            }
        } catch (SQLException ex) {
            Log.e(LOG_TAG,ex.getMessage());
        }

        if (conn!=null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Log.e(LOG_TAG,ex.getMessage());
            }
        }
        return result;
    }

    @Override
    public int numberOfRows() {
        int result=-1;
        String sql="SELECT Count(*) FROM "+FILES_TABLE_NAME;
        Connection conn=null;
        try {
            conn = connect();
            Statement stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery(sql);
            if (rs.next()) {
                result = rs.getInt(1);
            }

        } catch (SQLException ex) {
            Log.e(LOG_TAG,ex.getMessage());
        }

        if (conn!=null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Log.e(LOG_TAG,ex.getMessage());
            }
        }
        return result;
    }

    @Override
    public int numberOfUploadingRows() {
        int result=-1;
        String sql="SELECT Count(*) FROM "+UPLOADING_TABLE_NAME;
        Connection conn=null;
        try {
            conn = connect();
            Statement stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery(sql);
            if (rs.next()) {
                result = rs.getInt(1);
            }

        } catch (SQLException ex) {
            Log.e(LOG_TAG,ex.getMessage());
        }

        if (conn!=null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Log.e(LOG_TAG,ex.getMessage());
            }
        }
        return result;
    }

    @Override
    public boolean isUploading(String path) {
        int result=-1;
        String sql="SELECT Count(*) FROM "+UPLOADING_TABLE_NAME+" where "+FILES_COLUMN_PATH+"=\""+path+"\"";
        Connection conn=null;
        try {
            conn = connect();
            Statement stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery(sql);
            if (rs.next()) {
                result = rs.getInt(1);
            }

        } catch (SQLException ex) {
            Log.e(LOG_TAG,ex.getMessage());
        }

        if (conn!=null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Log.e(LOG_TAG,ex.getMessage());
            }
        }
        if (result > 0) return true;
        else return false;
    }


    @Override
    public ArrayList<String> getAllUploadingFiles() {

        int n=numberOfRows();

        ArrayList<String> result=new ArrayList<String>();

        String sql="SELECT "+UPLOADING_TABLE_NAME+" FROM "+FILES_TABLE_NAME;

        Connection conn=null;

        try {
            conn = this.connect();
            if (conn!=null) {
                Statement stmt  = conn.createStatement();
                ResultSet rs    = stmt.executeQuery(sql);

                // loop through the result set
                while (rs.next()) {
                    result.add(rs.getString(FILES_COLUMN_PATH));
                }

            }
        } catch (SQLException ex) {
            Log.e(LOG_TAG,ex.getMessage());
        }

        if (conn!=null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Log.e(LOG_TAG,ex.getMessage());
            }
        }
        return result;
    }

        /*
    public void updateFileStatus(String path) {
        String sql="UPDATE " + FILES_TABLE_NAME + " SET " + STATUS_COLUMN + "=" + SENDING + " WHERE " + FILES_COLUMN_PATH + "=\"" + path + "\"";
        Connection conn=null;
        try {
            conn = connect();
            Statement stmt=conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException ex) {
            Log.e(LOG_TAG,ex.getMessage());
        }

        if (conn!=null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Log.e(LOG_TAG,ex.getMessage());
            }
        }
    }

    public boolean isFileStatic(String path) {
        int result=-1;
        String sql="SELECT " + STATUS_COLUMN + " FROM "+ FILES_TABLE_NAME + " WHERE " + FILES_COLUMN_PATH + "=\"" + path + "\"";
        System.out.println("QUERY: " + sql);
        Connection conn=null;
        try {
            conn = connect();
            Statement stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery(sql);
            if (rs.next()) {
                System.out.println("OK");
                result = rs.getInt(1);
            }

        } catch (SQLException ex) {
            Log.e(LOG_TAG,ex.getMessage());
        }

        if (conn!=null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Log.e(LOG_TAG,ex.getMessage());
            }
        }
        System.out.println("RESULT: " + result);
        System.out.println("STATIC: " + Integer.parseInt(STATIC));
        if (result == Integer.parseInt(STATIC))
            return true;
        else
            return false;
    }
    */

    private Connection connect() throws SQLException {
        // SQLite connection string
        Connection conn = null;
        conn = DriverManager.getConnection(url);
        return conn;
    }
}
