package graphtheory;

import graphtheory.Structure.Graph;
import mytools.Config;
import driver.work.RetInfo;

import java.util.Random;

/**
 * The algorithm to solve the QGST problem.
 * @Author qkoqhh
 * @Date 2020-11-9
 */
abstract public class Algorithm {
    final static protected double inf=Double.MAX_VALUE;
    final static protected int MAXINT=Integer.MAX_VALUE/100;
    final static protected double eps=1e-8;
    final protected Random rand=new Random(2333);

    /**
     * Timeout parameter
     */
    public long start;
    static protected final long timeout= Config.timeout>0?Config.timeout*1000:Long.MAX_VALUE;

    protected Graph G;
    protected Query Q;

    public Algorithm(Graph G){
        this.G=G;
    }

    public void set_query(Query Q){
        this.Q=Q;
    }

    public RetInfo run(){
        RetInfo ret=new RetInfo();
        start=System.currentTimeMillis();
        ret.ans=solve();
        ret.time=System.currentTimeMillis()-start;
        return ret;
    }
    public abstract Structure.Tree solve();
}
