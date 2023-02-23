package mytools;

import driver.ProcessBase;

import java.lang.reflect.Field;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Enabled by config option "record each query"
 * When the option enabled, we first create a database ahead. The class @Info will be parsed and the database will generate the column according to the variable.
 * And then after each experiment, we fill the class and store in the database.
 * @Date 2020-12-28
 * @Author qkoqhh
 */
public class Info {
    /**
     * Something for connecting to database
     */
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String IP=Config.IP;
    static final String DB_URL = "jdbc:mysql://"+IP+":"+ ProcessBase.PORT +"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static final String EPOCH_IP=Config.IP;
    static final String EPOCH_PORT=Config.PORT;
    static final String EPOCH_URL = "jdbc:mysql://"+EPOCH_IP+":"+ EPOCH_PORT +"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    /**
     * username and password for database login
     */
    static final String USER = Config.USER;
    static final String PASS = Config.PASS;
    static final String dbname="info";
    static String tablename;
    static Connection conn;


    /**
     * The information to record
     */
    public int epoch;
    public String graphname;
    public double alpha;

    public String algorithm;
    public int data_id;
    public int g;
    public double runtime;
    public double cost;
    public List<Integer> anstree;
    public String date;
    /**
     * record other information, for example the way to generate query(default fixed query)
     */
    public String comment;

    String type(String s){
        if(s.contains(".")){
            return "varchar(1024)";
        }
        return s;
    }

    public void init(){
        try {
            System.out.println("Getting current epoch...");
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(EPOCH_URL,USER,PASS);
            Statement stmt=conn.createStatement();
            ResultSet ret=stmt.executeQuery("select epoch from info.epoch");
            if (ret.next()) {
                epoch = ret.getInt("epoch") + 1;
            }
            ret.close();
            System.out.println("Epoch: "+epoch);
            stmt.executeUpdate("update info.epoch set epoch=epoch+1");
            stmt.close();
            conn.close();

            System.out.println("Connect to database "+dbname+"...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            stmt=conn.createStatement();
            stmt.executeUpdate("use "+dbname);
            if(Config.expr==-1) {
                ret = stmt.executeQuery("select count(*) from information_schema.tables where `table_schema`='info'");
                if (ret.next()) {
                    tablename = "info_" + ret.getInt("count(*)");
                }
                Class c=this.getClass();
                Field[] fields=c.getFields();
                String sql="create table "+ tablename + "(";
                for(Field field:fields){
                    sql=sql+field.getName()+" "+type(field.getType().getTypeName())+",";
                }
                sql=sql.substring(0,sql.length()-1)+")";
                stmt.executeUpdate(sql);
            }else{
                tablename="info_"+Config.expr;
            }
            stmt.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        graphname=Config.database;

        comment="";
        if(Config.special_query){
            comment+="Special query: "+Config.query_file_name+";";
        }
        if(Config.special_fixed_query){
            comment+="Special fixed query: "+Config.query_id+";";
        }
        if(Config.random_query){
            comment+="Random query;";
        }
        if(Config.fixed_file_query){
            comment+="Fixed file query;";
        }
        if(Config.woPR){
            comment+="w/o PR;";
        }
        if(Config.woPP){
            comment+="w/o PP;";
        }
        if(Config.woPI){
            comment+="w/o PI;";
        }
    }
    public void add(){
        date=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
        Class c=this.getClass();
        Field[] fields=c.getFields();
        try {
            String sql="insert into "+tablename+" values (";
            for(Field field:fields){
                if(field.getType()==boolean.class) {
                    sql = sql + "\"" + ((Boolean)field.get(this)?1:0) + "\",";
                }else {
                    sql = sql + "\"" + field.get(this).toString() + "\",";
                }
            }
            sql=sql.substring(0,sql.length()-1)+")";
            Statement stmt=conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (IllegalAccessException | SQLException e) {
            e.printStackTrace();
        }
    }
}
