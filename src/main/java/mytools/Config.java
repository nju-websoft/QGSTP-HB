package mytools;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Config {
    public static boolean debug;

    public static int worker;
    public static String IP;
    public static String PORT;
    public static String USER;
    public static String PASS;
    public static String database;
    public static String SD;
    public static String result_file;

    public static List<Double>alpha_array=new ArrayList<>();

    public static int expr;

    public static int timeout;
    public static boolean com_more;

    public static boolean woPR;
    public static boolean woPP;
    public static boolean woPI;

    public static boolean random_query;
    public static int query_num;

    public static boolean fixed_query;
    public static boolean fixed_file_query;
    public static boolean record_each_query;

    public static boolean special_query;
    public static String query_file_name;

    public static boolean special_fixed_query;
    public static int query_id;

    public static boolean[] algorithm =new boolean[20];


    static boolean isTrue(String bool) throws Exception {
        if(bool.equals("TRUE"))return true;
        else if(bool.equals("FALSE"))return false;
        else throw new Exception("Bool Value Error");
    }

    public static String config_filename="config.properties";

    public static void init(String[]args){
    }

    public static void init() throws Exception {
        Properties properties=new Properties();
        try{
            InputStream in=Config.class.getClassLoader().getResourceAsStream(config_filename);
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        debug=isTrue(properties.getProperty("DEBUG"));
        worker=Integer.parseInt(properties.getProperty("WORKER"));
        IP=properties.getProperty("IP");
        PORT=properties.getProperty("PORT");
        USER=properties.getProperty("USER");
        PASS=properties.getProperty("PASS");
        database=properties.getProperty("DATABASE");
        SD=properties.getProperty("SD");
        result_file=properties.getProperty("RESULT_FILE");
        String[]alpha_list=properties.getProperty("ALPHA").split(",");
        for(String str:alpha_list){
            alpha_array.add(Double.parseDouble(str));
        }
        expr=Integer.parseInt(properties.getProperty("EXPR"));
        timeout=Integer.parseInt(properties.getProperty("TIMEOUT"));
        com_more=isTrue(properties.getProperty("COM_MORE"));
        woPR=isTrue(properties.getProperty("woPR"));
        woPP=isTrue(properties.getProperty("woPP"));
        woPI=isTrue(properties.getProperty("woPI"));
        random_query=isTrue(properties.getProperty("RANDOM_QUERY"));
        query_num=Integer.parseInt(properties.getProperty("QUERY_NUM"));
        fixed_query =isTrue(properties.getProperty("FIXED_QUERY"));
        fixed_file_query =isTrue(properties.getProperty("FIXED_FILE_QUERY"));
        record_each_query=isTrue(properties.getProperty("RECORD_EACH_QUERY"));
        special_query=isTrue(properties.getProperty("SPECIAL_QUERY"));
        query_file_name=properties.getProperty("QUERY_FILE_NAME");
        special_fixed_query=isTrue(properties.getProperty("SPECIAL_FIXED_QUERY"));
        query_id=Integer.parseInt(properties.getProperty("QUERY_ID"));
        String[]algorithm_list=properties.getProperty("ALGORITHM").split(",");
        for(String str:algorithm_list){
            algorithm[Integer.parseInt(str)]=true;
        }
    }



}
