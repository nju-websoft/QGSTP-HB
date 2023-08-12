package driver;

import graphtheory.Query;
import graphtheory.Structure.*;
import graphtheory.semantic_distance.HardCoded;
import graphtheory.semantic_distance.Rdf2Vec_angular;
import mytools.Config;
import graphtheory.hub.Hub;

import java.sql.*;
import java.util.LinkedList;

import static graphtheory.Structure.alpha;

abstract public class ProcessBase {
    // JDBC Driver and Database URL
    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String IP=Config.IP;
    public static final String PORT=Config.PORT;
    public static final String DB_URL = "jdbc:mysql://"+IP+":"+PORT+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true&tcpKeepAlive=true&rewriteBatchedStatements=true";
    // username and password for database login
    public static final String USER = Config.USER;
    public static final String PASS = Config.PASS;
    public static final String ENGINE="archive";

    protected static final double inf=Double.MAX_VALUE;
    protected static final double eps=1e-8;

    // connection to database
    protected Connection conn;

    protected final String dbname;

    protected ProcessBase(String graphname) {
        dbname=graphname;
    }

    public void connect(){
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("Connect to database "+dbname+"...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            Statement stmt=conn.createStatement();
            stmt.executeUpdate("use "+dbname);
            stmt.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }


    public void close() throws SQLException {
        conn.close();
    }


    public Graph G;
    public Query Q;

    /**
     * Read the graph from database to @G
     * Please don't change @G after reading the graph
     * Before reading graph, please call @connect()
     * @throws SQLException
     */
    public void read_graph() throws Exception {
        System.out.println("Reading graph...");
        conn.setAutoCommit(false);
        conn.setReadOnly(true);
        G=new Graph();
        Statement stmt=conn.createStatement();
        ResultSet ret;
        ret=stmt.executeQuery("select count(*) from nodes");
        if(ret.next()){
            G.n=ret.getInt("count(*)");
        }
        conn.commit();
        ret.close();
        stmt.close();

        PreparedStatement pstmt=conn.prepareStatement("select id,weight from nodes",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        pstmt.setFetchSize(Integer.MIN_VALUE);
        pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
        ret=pstmt.executeQuery();
        G.a=new double[G.n];
        for(int i=0;i<G.n;i++){
            G.edges.add(new LinkedList<>());
        }

        while(ret.next()){
            int id=ret.getInt("id");
            G.a[id]=ret.getDouble("weight");
        }
        conn.commit();
        ret.close();
        pstmt.close();

        pstmt=conn.prepareStatement("select * from edges",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
        pstmt.setFetchSize(Integer.MIN_VALUE);
        pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
        ret=pstmt.executeQuery();
        while(ret.next()){
            G.m++;
            int x=ret.getInt("x");
            int y=ret.getInt("y");
            G.edges.get(x).add(new Hop(y));G.edges.get(y).add(new Hop(x));
        }
        conn.commit();
        ret.close();
        pstmt.close();

        switch (Config.SD){
            case "Rdf2Vec": G.sd=new Rdf2Vec_angular(conn,G.n);break;
            case "HardCode": G.sd=new HardCoded(conn,G.n);break;
            default: System.err.println("Notice: No SD function found");
        }
        set_edge_weight();

        read_hub_label();

        conn.setReadOnly(false);
        conn.setAutoCommit(true);
    }

    private int subgraph_find(int x){
        if(G.f[x]==x){
            return x;
        }
        return G.f[x]=subgraph_find(G.f[x]);
    }
    void read_hub_label() throws Exception {
        ResultSet ret;
        PreparedStatement pstmt;
        Statement stmt=conn.createStatement();


        if (!Config.woPR||!Config.woPI){
            System.out.println("Reading hub label hop...");

            stmt=conn.createStatement();
            ret=stmt.executeQuery("select count(*) from hub_hop");
            if(ret.next()){
                long tot=ret.getLong("count(*)");
                G.hub_hop=new Hub(G.n,tot,new Object[]{0,0});
            }
            conn.commit();
            ret.close();
            stmt.close();

            pstmt=conn.prepareStatement("select * from hub_hop",ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            pstmt.setFetchSize(Integer.MIN_VALUE);
            pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
            ret=pstmt.executeQuery();
            while(ret.next()){
                if(ret.getInt("u")!=G.hub_hop.cnt){
                    G.hub_hop.nextcursor();
                }
                G.hub_hop.addInt(ret.getInt("v"));
                G.hub_hop.addInt(ret.getInt("w"));
                G.hub_hop.addsize();
            }
            conn.commit();
            ret.close();
            pstmt.close();
        }

    }

    void set_edge_weight(){
        System.out.println("Setting edge weight...");
        for(int i=0;i<G.n;i++){
            for(Hop j:G.edges.get(i)){
                j.w=G.sd.cal(j.t,i);
            }
        }
    }

    protected void read_hub_mix(){
        if(Config.woPR){
            return;
        }
        final int a= (int) (alpha*10);
        try {
            ResultSet ret;
            PreparedStatement pstmt;
            Statement stmt = conn.createStatement();
            conn.setAutoCommit(false);
            ret = stmt.executeQuery("select count(*) from hub_mix_"+a);
            if (ret.next()) {
                long tot = ret.getLong("count(*)");
                G.hub_mix = new Hub(G.n, tot, new Object[]{0, 0D});
            }
            conn.commit();
            ret.close();

            System.out.println("Reading hub label mix...");
            pstmt = conn.prepareStatement("select * from hub_mix_"+a, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            pstmt.setFetchSize(Integer.MIN_VALUE);
            pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
            ret = pstmt.executeQuery();
            while (ret.next()) {
                if (ret.getInt("u") != G.hub_mix.cnt) {
                    G.hub_mix.nextcursor();
                }
                G.hub_mix.addInt(ret.getInt("v"));
                G.hub_mix.addDouble(ret.getDouble("w"));
                G.hub_mix.addsize();
            }
            conn.commit();
            ret.close();
            pstmt.close();
            conn.setAutoCommit(true);
        } catch (SQLException throwables) {
            System.err.println("???");
            throwables.printStackTrace();
        } catch (Exception e) {
            System.err.println("???");
            e.printStackTrace();
        }
    }
}
