package driver.data;

import graphtheory.Structure.*;
import graphtheory.hub.*;
import mytools.Config;
import org.jgrapht.alg.util.Pair;
import driver.ProcessBase;

import java.util.*;

import static graphtheory.Structure.alpha;
import static graphtheory.Structure.beta;
import static java.lang.Math.abs;
import static java.lang.Math.min;

public class TestHub extends ProcessBase {
    protected TestHub(String graphname) throws Exception {
        super(graphname);
        connect();
        read_graph();
    }
    Random rand=new Random();


    double[]d;
    boolean[]v;
    int[]g;
    void bfs(int s){
        Queue<Integer>q=new LinkedList<>();
        g=new int[G.n];
        q.add(s);
        g[s]=1;
        while(!q.isEmpty()){
            int t=q.poll();
            for (Hop j:G.edges.get(t)){
                if(g[j.t]==0){
                    g[j.t]=g[t]+1;
                    q.add(j.t);
                }
            }
        }
        for (int i=0;i<G.n;i++){
            g[i]--;
        }
    }

    int query_hop(int s,int t){
        int ans=Integer.MAX_VALUE;
        for (HubHopIter x=new HubHopIter(G.hub_hop,s),y=new HubHopIter(G.hub_hop,t);x.hasNext()&&y.hasNext();){
            if (x.v==y.v){
                ans=min(ans,x.w+y.w);
                x.next();
                y.next();
            }else if(x.v<y.v){
                x.next();
            }else{
                y.next();
            }
        }
        return ans;
    }

    void source_sample_hop(int n,int m){
        for (int i=0;i<n;i++){
            int s=rand();
            bfs(s);
            for (int j=0;j<m;j++){
                int t=rand();
                if(g[t]!=query_hop(s,t)){
                    System.err.println(s+" "+t);
                    System.err.println("Hub Label hop Wrong!!!! "+"The real distance is "+g[t]+" while opt5 calculate "+query_hop(s,t));
                }
            }
        }
    }



    int rand(){return abs(rand.nextInt())%G.n;}
    void test_runtime(int m){
        long start;
        if(!Config.woPR) {
            start = System.currentTimeMillis();
            for (int i = 0; i < m; i++) {
                query_hop(rand(), rand());
            }
            System.out.println("Time hop: " + (System.currentTimeMillis() - start) / 1000D / m + " ms");
        }

    }



    void dij_mix(int s){
        PriorityQueue<Pair<Integer,Double>>q=new PriorityQueue<>(Comparator.comparing(Pair::getSecond));
        d=new double[G.n];
        v=new boolean[G.n];
        Arrays.fill(d,inf);
        d[s]=alpha*G.a[s];q.add(new Pair<>(s,d[s]));
        while(!q.isEmpty()){
            int t=q.poll().getFirst();
            if(v[t]){
                continue;
            }
            v[t]=true;
            for(Hop j:G.edges.get(t)){
                if(d[j.t]>d[t]+alpha*G.a[j.t]+beta/2*j.w){
                    d[j.t]=d[t]+alpha*G.a[j.t]+beta/2*j.w;
                    q.add(new Pair<>(j.t,d[j.t]));
                }
            }
        }
    }

    double query_mix(int s, int t){
        double ans=inf;
        for (HubMixIter x = new HubMixIter(G.hub_mix,s),y=new HubMixIter(G.hub_mix,t); x.hasNext()&&y.hasNext();){
            if(x.v==y.v){
                ans=min(ans,x.w+y.w-alpha*G.a[x.v]);
                x.next();
                y.next();
            }else if(x.v<y.v){
                x.next();
            }else{
                y.next();
            }
        }
        return ans;
    }

    void source_sample_mix(int n,int m) throws Exception {
        System.out.println("Testing Hub Mix...");
        read_hub_mix();
        for(int i=0;i<n;i++){
            int s= rand();
            dij_mix(s);
            for(int j=0;j<m;j++){
                int t=rand();
                if(Math.abs(d[t]-query_mix(s,t))>eps){
                    System.err.println(s+" "+t);
                    System.err.println("Hub Label mix Wrong!!!! "+"The real distance is "+d[t]+" while opt5 calculate "+query_mix(s,t));
                }
            }
        }
    }
}
