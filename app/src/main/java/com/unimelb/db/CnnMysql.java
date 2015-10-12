package com.unimelb.db;

/**
 * Created by yingying on 8/30/2015.
 */
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import static com.unimelb.utils.mp3Utils.getCurrTime;
import static com.unimelb.utils.mp3Utils.string2bigint;
import static java.lang.Thread.sleep;

public class  CnnMysql{
    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    // Mysql database
    //private static final String DB_URL = "jdbc:mysql://127.0.0.1/mp3_app_data";
    private static final String DB_URL = "jdbc:mysql://49.213.15.196/mp3_app_data";

    //  Database credentials
    private static final String USER = "serena";
    private static final String PASS = "WYYpll08040408";
    private static final String TBL_TEMP = "tbl_temperature";
    private static final String TBL_ACC = "tbl_accelerometer";
    private static final String DB = "app_data";

    private  Connection mysql = null;
    private Statement stmt = null;

    public CnnMysql() {

       /* Auto commit updated data.*/
       getMysqlConnection(false);
    }

    public void getMysqlConnection(Boolean autoCommit) {
        try {
            Class.forName(JDBC_DRIVER);
            if (mysql == null) {
                mysql = DriverManager.getConnection(DB_URL, USER, PASS);
                mysql.setAutoCommit(autoCommit);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param sql
     * @return
     * @throws SQLException
     */
    public ResultSet query(String sql) throws SQLException {
        getMysqlConnection(false);
        ResultSet rs = null;
        try {
            stmt = mysql.createStatement();
            rs = stmt.executeQuery(sql);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    /**
     * @param sql
     * @return
     */
    public Boolean execute(String sql) {
        getMysqlConnection(false);
        try {
            stmt = mysql.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     *
     * @param stime
     * @param etime
     * @return
     * @throws SQLException
     */
    public HashMap<BigInteger, Integer> readTemperature(BigInteger stime, BigInteger etime) throws SQLException {
        HashMap<BigInteger, Integer> tempData = null;
        String sql = "SELECT * FROM " + this.TBL_TEMP + " where submit_time >= " + stime + " and submit_time <= " + etime;
        ResultSet rs = query(sql);
        //System.out.println("begin to query " + sql);
        while(rs.next()) {
           BigInteger time = new BigInteger(rs.getString("submit_time"));
           Integer value = rs.getInt("value");
           System.out.println("time:"+time+", value:"+value);
        }
        return tempData;
    }

    /**
     * Insert a key value pair to mysql database.
     * @param submitTime
     * @param tmpValue
     * @return
     * @throws SQLException
     */
    public Boolean writeTemperature(BigInteger submitTime, int tmpValue) throws SQLException {
        PreparedStatement pstmt = null;
        boolean success = false;
        StringBuffer sql = new StringBuffer("INSERT INTO " + this.TBL_TEMP + "(submit_time, value) values ");
        sql.append(" (?, ?)");
        try {
            pstmt = mysql.prepareStatement(sql.toString());
            pstmt.setBigDecimal(1, new BigDecimal(submitTime));
            pstmt.setInt(2, tmpValue);
            pstmt.executeUpdate();
            mysql.commit();
            success = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return success;
    }

    /**
     *
     * @param rs
     * @throws SQLException
     */
    public void readData(ResultSet rs) throws SQLException {
        try {
            while(rs.next()) {
                //Retrieve by column name
             /*step1: read data from usb,physical device */
                String name = rs.getString("name");
                String tel = rs.getString("tel");
                System.out.println("name:"+name);
                System.out.println("tel:"+tel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * close Mysql connecton.
     */
    public void close() {
        try {
            if(stmt != null) {
                stmt.close();
            }
            if (mysql != null) {
                mysql.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
    }//end main

}//end