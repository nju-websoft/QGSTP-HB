package driver.data;

import graphtheory.Structure.*;
import mytools.Config;
import mytools.Debug;
import mytools.SQL_batch;
import org.jgrapht.alg.util.Pair;
import driver.ProcessBase;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static graphtheory.Structure.alpha;
import static graphtheory.Structure.beta;
import static java.lang.Math.min;

/**
 * Generate Hub Label for each graph
 * For each graph, we generate two kinds of labels_list, one for graph based on node weight(sal), another based on edge weight(sd)
 * @Author qkoqhh
 * @Date 2020-12-14
 */
public class GenerateHubLabel extends ProcessBase {

    public GenerateHubLabel(String graphname) {
        super(graphname);
        try {
            connect();
            read_graph();
            close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    List<List<Hop>>hub_sd,hub_sal,hub_mix;
    List<List<Pair<Integer,Integer>>>hub_hop;

    int[]rank;
    double query_sal(int a,int b){
        for(Hop i:hub_sal.get(b)){
            if(i.t==a){
                return i.w;
            }
        }
        for(Hop i:hub_sal.get(a)){
            if(i.t==b){
                return i.w;
            }
        }
        int n=hub_sal.get(a).size(),m=hub_sal.get(b).size();
        double ans=inf;
        for(int x=0,y=0;x<n&&y<m;){
            int l=hub_sal.get(a).get(x).t,r=hub_sal.get(b).get(y).t;
            if(l==r){
                ans=min(ans,hub_sal.get(a).get(x).w+hub_sal.get(b).get(y).w-G.a[l]);
                x++;y++;
            }else if(rank[l]<rank[r]){
                x++;
            }else{
                y++;
            }
        }
        return ans;
    }
    double query_sd(int a,int b){
        for(Hop i:hub_sd.get(b)){
            if(i.t==a){
                return i.w;
            }
        }
        for(Hop i:hub_sd.get(a)){
            if(i.t==b){
                return i.w;
            }
        }
        int n=hub_sd.get(a).size(),m=hub_sd.get(b).size();
        double ans=inf;
        for(int x=0,y=0;x<n&&y<m;){
            int l=hub_sd.get(a).get(x).t,r=hub_sd.get(b).get(y).t;
            if(l==r){
                ans=min(ans,hub_sd.get(a).get(x).w+hub_sd.get(b).get(y).w);
                x++;y++;
            }else if(rank[l]<rank[r]){
                x++;
            }else{
                y++;
            }
        }
        return ans;
    }
    int query_hop(int a,int b){
        for (Pair<Integer,Integer> i:hub_hop.get(b)){
            if(i.getFirst().equals(a)){
                return i.getSecond();
            }
        }
        for (Pair<Integer,Integer> i: hub_hop.get(a)){
            if(i.getFirst().equals(b)){
                return i.getSecond();
            }
        }
        int n=hub_hop.get(a).size(),m= hub_hop.get(b).size();
        int ans=Integer.MAX_VALUE;
        for (int x=0,y=0;x<n&&y<m;){
            int l=hub_hop.get(a).get(x).getFirst(),r=hub_hop.get(b).get(y).getFirst();
            if(l==r){
                ans=min(ans,hub_hop.get(a).get(x).getSecond()+hub_hop.get(b).get(y).getSecond());
                x++;y++;
            }else if(rank[l]<rank[r]){
                x++;
            }else{
                y++;
            }
        }
        return ans;
    }
    double query_mix(int a,int b){
        for(Hop i:hub_mix.get(b)){
            if(i.t==a){
                return i.w;
            }
        }
        for(Hop i:hub_mix.get(a)){
            if(i.t==b){
                return i.w;
            }
        }
        int n=hub_mix.get(a).size(),m=hub_mix.get(b).size();
        double ans=inf;
        for(int x=0,y=0;x<n&&y<m;){
            int l=hub_mix.get(a).get(x).t,r=hub_mix.get(b).get(y).t;
            if(l==r){
                ans=min(ans,hub_mix.get(a).get(x).w+hub_mix.get(b).get(y).w-alpha*G.a[l]);
                x++;y++;
            }else if(rank[l]<rank[r]){
                x++;
            }else{
                y++;
            }
        }
        return ans;
    }
    int num;
    int cnt_i;

    double[]d;
    boolean[]v;
    List<Integer>vis;
    void dij_sal(int s){
        PriorityQueue<Pair<Integer,Double>>q=new PriorityQueue<>(Comparator.comparing(Pair::getSecond));
        d[s]=G.a[s];
        q.add(new Pair<>(s,d[s]));
        vis=new LinkedList<>();
        vis.add(s);
        int count=0;
        while(!q.isEmpty()){
            int t=q.poll().getFirst();
            if(v[t]){
                continue;
            }
            v[t]=true;
            if(query_sal(s,t)<=d[t]+eps){
                continue;
            }
            hub_sal.get(t).add(new Hop(s,d[t]));
            num++;count++;
            for(Hop j:G.edges.get(t)){
                if(d[j.t]>d[t]+G.a[j.t]){
                    d[j.t]=d[t]+G.a[j.t];
                    q.add(new Pair<>(j.t,d[j.t]));
                    vis.add(j.t);
                }
            }
        }
        for(Integer i:vis){
            d[i]=inf;
            v[i]=false;
        }
        Debug.print(cnt_i+"th node     "+"Add Hub_sal "+ count+".  Total: "+num);
    }
    void dij_sd(int s){
        PriorityQueue<Pair<Integer,Double>>q=new PriorityQueue<>(Comparator.comparing(Pair::getSecond));
        d[s]=0;
        q.add(new Pair<>(s,d[s]));
        vis=new LinkedList<>();
        vis.add(s);
        int count=0;
        while(!q.isEmpty()){
            int t=q.poll().getFirst();
            if(v[t]){
                continue;
            }
            v[t]=true;
            if(query_sd(s,t)<=d[t]+eps){
                //System.out.println("Cut!!!");
                continue;
            }
            hub_sd.get(t).add(new Hop(s,d[t]));
            num++;
            count++;
            for(Hop j:edges.get(t)){
                if(d[j.t]>d[t]+j.w){
                    d[j.t]=d[t]+j.w;
                    q.add(new Pair<>(j.t,d[j.t]));
                    vis.add(j.t);
                }
            }
        }
        for(Integer i:vis){
            d[i]=inf;
            v[i]=false;
        }
        Debug.print(cnt_i+"th node     "+"Add Hub_sd "+ count+".  Total: "+num);
    }
    void dij_mix(int s){
        PriorityQueue<Pair<Integer,Double>>q=new PriorityQueue<>(Comparator.comparing(Pair::getSecond));
        d[s]=alpha*G.a[s];
        q.add(new Pair<>(s,d[s]));
        vis=new LinkedList<>();
        vis.add(s);
        int count=0;
        while(!q.isEmpty()){
            int t=q.poll().getFirst();
            if(v[t]){
                continue;
            }
            v[t]=true;
            if(query_mix(s,t)<=d[t]+eps){
                continue;
            }
            hub_mix.get(t).add(new Hop(s,d[t]));
            num++;count++;
            for(Hop j:G.edges.get(t)){
                if(d[j.t]>d[t]+alpha*G.a[j.t]+beta/2*j.w){
                    d[j.t]=d[t]+alpha*G.a[j.t]+beta/2*j.w;
                    q.add(new Pair<>(j.t,d[j.t]));
                    vis.add(j.t);
                }
            }
        }
        for(Integer i:vis){
            d[i]=inf;
            v[i]=false;
        }
        Debug.print(cnt_i+"th node     "+"Add Hub_mix "+ count+".  Total: "+num);
    }



    double[]bc;
    void cal_bc_sal(int s){
        PriorityQueue<Pair<Integer,Double>>q=new PriorityQueue<>(Comparator.comparing(Pair::getSecond));
        v=new boolean[G.n];
        d=new double[G.n];
        int[]p=new int[G.n];
        double[]delta=new double[G.n];
        Arrays.fill(d,inf);
        d[s]=G.a[s];q.add(new Pair<>(s,d[s]));
        p[s]=1;
        List<Integer>[] pre=new List[G.n];
        Stack<Integer>stack=new Stack<>();
        while(!q.isEmpty()){
            int t=q.poll().getFirst();
            if(v[t]){
                continue;
            }
            v[t]=true;
            stack.add(t);
            for(Hop j:G.edges.get(t)){
                if(d[j.t]>d[t]+G.a[j.t]){
                    d[j.t]=d[t]+G.a[j.t];
                    pre[j.t]=new LinkedList<>();
                    pre[j.t].add(t);
                    p[j.t]=p[t];
                    q.add(new Pair<>(j.t,d[j.t]));
                }else if(Math.abs(d[j.t]-d[t]-G.a[j.t])<eps){
                    pre[j.t].add(t);
                    p[j.t]+=p[t];
                }
            }
        }
        while(stack.size()>1){
            int t=stack.pop();
            for(Integer j:pre[t]){
                if(j!=s) {
                    delta[j] += (1D + delta[t]) * p[j] / p[t];
                }
            }
        }
        for(int i=0;i<G.n;i++){
            bc[i]+=delta[i];
        }
    }
    void cal_bc_sd(int s){
        PriorityQueue<Pair<Integer,Double>>q=new PriorityQueue<>(Comparator.comparing(Pair::getSecond));
        v=new boolean[n];
        d=new double[n];
        int[]p=new int[n];
        double[]delta=new double[n];
        Arrays.fill(d,inf);
        d[s]=0;q.add(new Pair<>(s,d[s]));
        p[s]=1;
        List<Integer>[] pre=new List[n];
        Stack<Integer>stack=new Stack<>();
        while(!q.isEmpty()){
            int t=q.poll().getFirst();
            if(v[t]){
                continue;
            }
            v[t]=true;
            stack.add(t);
            for(Hop j:edges.get(t)){
                if(d[j.t]>d[t]+j.w){
                    d[j.t]=d[t]+j.w;
                    pre[j.t]=new LinkedList<>();
                    pre[j.t].add(t);
                    p[j.t]=p[t];
                    q.add(new Pair<>(j.t,d[j.t]));
                }else if(Math.abs(d[j.t]-d[t]-j.w)<eps){
                    pre[j.t].add(t);
                    p[j.t]+=p[t];
                }
            }
        }
        while(stack.size()>1){
            int t=stack.pop();
            for(Integer j:pre[t]){
                if(j!=s){
                    delta[j]+=(1+delta[t])*p[j]/p[t];
                }
            }
        }
        p[s]=0;
        for(int i=0;i<n;i++){
            bc[i]+=delta[i];
        }
    }
    void cal_bc_mix(int s){
        PriorityQueue<Pair<Integer,Double>>q=new PriorityQueue<>(Comparator.comparing(Pair::getSecond));
        v=new boolean[G.n];
        d=new double[G.n];
        int[]p=new int[G.n];
        double[]delta=new double[G.n];
        Arrays.fill(d,inf);
        d[s]=alpha*G.a[s];q.add(new Pair<>(s,d[s]));
        p[s]=1;
        List<Integer>[] pre=new List[G.n];
        Stack<Integer>stack=new Stack<>();
        while(!q.isEmpty()){
            int t=q.poll().getFirst();
            if(v[t]){
                continue;
            }
            v[t]=true;
            stack.add(t);
            for(Hop j:G.edges.get(t)){
                final double new_dis=d[t]+alpha*G.a[j.t]+beta/2*j.w;
                if(d[j.t]>new_dis){
                    d[j.t]=new_dis;
                    pre[j.t]=new LinkedList<>();
                    pre[j.t].add(t);
                    p[j.t]=p[t];
                    q.add(new Pair<>(j.t,d[j.t]));
                }else if(Math.abs(d[j.t]-new_dis)<eps){
                    pre[j.t].add(t);
                    p[j.t]+=p[t];
                }
            }
        }
        while(stack.size()>1){
            int t=stack.pop();
            for(Integer j:pre[t]){
                if(j!=s) {
                    delta[j] += (1D + delta[t]) * p[j] / p[t];
                }
            }
        }
        for(int i=0;i<G.n;i++){
            bc[i]+=delta[i];
        }
    }

    static final int selnum=1000;

    public void solve_sal() {
        num=0;
        List<Integer> enumeration=new ArrayList<>();
        for(int i=0;i<G.n;i++){
            enumeration.add(i);
        }
        enumeration.sort(Comparator.comparing(o->-G.edges.get(o).size()));
        if(Config.database.startsWith("dbpedia")) {
            bc = new double[G.n];
            for (int i = 0; i < selnum; i++) {
                Debug.print("BC sal node: " + i);
                cal_bc_sal(enumeration.get(i));
            }

            enumeration.sort(Comparator.comparing(o -> -bc[o]));
        }


        rank=new int[G.n];
        hub_sal=new ArrayList<>();
        d=new double[G.n];
        Arrays.fill(d,inf);
        v=new boolean[G.n];
        for(int i=0;i<G.n;i++){
            rank[enumeration.get(i)]=i;
            hub_sal.add(new ArrayList<>());
        }
        cnt_i=0;
        for(Integer i:enumeration){
            cnt_i++;
            dij_sal(i);
        }

        try {
            connect();
            System.out.println("Writing Hub sal...");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(
                    "create table hub_sal(" +
                            "    u int," +
                            "    v int," +
                            "    w double" +
                            ")" +
                            "engine="+ENGINE);
            stmt.close();
            conn.setAutoCommit(false);
            SQL_batch batch=new SQL_batch(conn,"insert into hub_sal values (?,?,?)");
            long start=System.currentTimeMillis();
            for (int i = 0; i < G.n; i++) {
                hub_sal.get(i).sort(Comparator.comparing(o->o.t));
                for (Hop j : hub_sal.get(i)) {
                    batch.add(i, j.t, j.w);
                }
            }
            batch.close();
            stmt= conn.createStatement();
            stmt.close();
            long end=System.currentTimeMillis();
            System.out.println("Time sal: "+ (end-start));
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hub_sal=null;
    }

    List<List<Hop>>edges;
    int[]f;
    int n;
    int[]nodemap;
    int find(int x){
        if(f[x]==x){
            return x;
        }
        return f[x]=find(f[x]);
    }
    void compress(){
        f=new int[G.n];
        nodemap=new int[G.n];
        for(int i=0;i<G.n;i++){
            f[i]=i;
        }
        for(int i=0;i<G.n;i++){
            for(Hop j:G.edges.get(i)){
                if(j.w<eps){
                    int x=find(i),y=find(j.t);
                    if(x!=y){
                        f[x]=y;
                    }
                }
            }
        }
        for(int i=0;i<G.n;i++){
            if(find(i)==i){
                nodemap[i]=n;
                n++;
            }
        }
        for(int i=0;i<G.n;i++){
            if(find(i)!=i){
                nodemap[i]=nodemap[find(i)];
            }
        }
        List<Map<Integer,Double>>tmp_edges=new ArrayList<>();
        for(int i=0;i<n;i++){
            tmp_edges.add(new HashMap<>());
        }
        for(int i=0;i<G.n;i++){
            for(Hop j:G.edges.get(i)){
                if(nodemap[i]!=nodemap[j.t]){
                    if(!tmp_edges.get(nodemap[i]).containsKey(nodemap[j.t])||tmp_edges.get(nodemap[i]).get(nodemap[j.t])>j.w){
                        tmp_edges.get(nodemap[i]).put(nodemap[j.t],j.w);
                    }
                }
            }
        }
        edges=new ArrayList<>();
        for(int i=0;i<n;i++){
            edges.add(new ArrayList<>());
            for(Map.Entry<Integer,Double> entry:tmp_edges.get(i).entrySet()){
                edges.get(i).add(new Hop(entry.getKey(),entry.getValue()));
            }
        }
    }

    public void solve_sd(){
        num=0;
        compress();
        System.out.println("Compressed Graph nodes num: "+n);

        List<Integer> enumeration=new ArrayList<>();
        for(int i=0;i<n;i++){
            enumeration.add(i);
        }
        enumeration.sort(Comparator.comparing(o->-edges.get(o).size()));
        if(Config.database.startsWith("dbpedia")) {
            bc=new double[n];
            for (int i = 0; i < selnum; i++) {
                Debug.print("BC sd node: " + i);
                cal_bc_sd(enumeration.get(i));
            }

            enumeration.sort(Comparator.comparing(o -> -bc[o]));
        }

        rank=new int[n];
        hub_sd=new ArrayList<>();
        d=new double[n];
        Arrays.fill(d,inf);
        v=new boolean[n];
        for(int i=0;i<n;i++){
            rank[enumeration.get(i)]=i;
            hub_sd.add(new ArrayList<>());
        }
        cnt_i=0;
        for(Integer i:enumeration){
            cnt_i++;
            dij_sd(i);
        }

        int[]revnodemap=new int[n];
        for(int i=0;i<G.n;i++){
            if(find(i)==i){
                revnodemap[nodemap[i]]=i;
            }
        }
        try {
            connect();
            System.out.println("Writing Hub sd...");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(
                    "create table hub_sd(" +
                            "    u int," +
                            "    v int," +
                            "    w double" +
                            ")" +
                            "engine="+ENGINE);
            stmt.close();
            conn.setAutoCommit(false);
            long start=System.currentTimeMillis();
            SQL_batch batch=new SQL_batch(conn,"insert into hub_sd values (?,?,?)");
            for (int i = 0; i < G.n; i++){
                if(find(i)==i) {
                    hub_sd.get(nodemap[i]).sort(Comparator.comparing(o->revnodemap[o.t]));
                    for (Hop j : hub_sd.get(nodemap[i])) {
                        batch.add(i, revnodemap[j.t], j.w);
                    }
                }
            }
            batch.close();
            long end=System.currentTimeMillis();
            System.out.println("Time sd: "+ (end-start));
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hub_sd=null;
    }
    public void solve_mix() {
        num=0;
        List<Integer> enumeration=new ArrayList<>();
        for(int i=0;i<G.n;i++){
            enumeration.add(i);
        }
        enumeration.sort(Comparator.comparing(o->-G.edges.get(o).size()));
        if(Config.database.startsWith("dbpedia")) {
            bc = new double[G.n];
            for (int i = 0; i < selnum; i++) {
                Debug.print("BC mix node: " + i);
                cal_bc_mix(enumeration.get(i));
            }

            enumeration.sort(Comparator.comparing(o -> -bc[o]));
        }



        rank=new int[G.n];
        hub_mix=new ArrayList<>();
        d=new double[G.n];
        Arrays.fill(d,inf);
        v=new boolean[G.n];
        for(int i=0;i<G.n;i++){
            rank[enumeration.get(i)]=i;
            hub_mix.add(new ArrayList<>());
        }
        cnt_i=0;
        for(Integer i:enumeration){
            cnt_i++;
            dij_mix(i);
        }

        try {
            connect();
            System.out.println("Writing Hub Mix ("+alpha+")...");
            Statement stmt = conn.createStatement();
            final int a= (int) (alpha*10);
            stmt.executeUpdate(
                    "create table hub_mix_"+a+"(" +
                            "    u int," +
                            "    v int," +
                            "    w double" +
                            ")" +
                            "engine="+ENGINE);
            stmt.close();
            conn.setAutoCommit(false);
            SQL_batch batch=new SQL_batch(conn,"insert into hub_mix_"+a+" values (?,?,?)");
            long start=System.currentTimeMillis();
            for (int i = 0; i < G.n; i++) {
                hub_mix.get(i).sort(Comparator.comparing(o->o.t));
                for (Hop j : hub_mix.get(i)) {
                    batch.add(i, j.t, j.w);
                }
            }
            batch.close();
            long end=System.currentTimeMillis();
            System.out.println("Time mix: "+ (end-start));
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hub_mix=null;
    }


    void cal_bc_hop(int s){
        Queue<Integer>q=new LinkedList<>();
        v=new boolean[G.n];
        int[] d=new int[G.n];
        int[] p=new int[G.n];
        double[]delta=new double[G.n];
        d[s]=0;q.add(s);
        p[s]=1;
        v[s]=true;
        List<Integer>[] pre=new List[G.n];
        Stack<Integer>stack=new Stack<>();
        while(!q.isEmpty()){
            int t=q.poll();
            stack.add(t);
            for(Hop j:G.edges.get(t)){
                if(!v[j.t]){
                    d[j.t]=d[t]+1;
                    pre[j.t]=new LinkedList<>();
                    pre[j.t].add(t);
                    p[j.t]=p[t];
                    if(!v[j.t]) {
                        q.add(j.t);
                        v[j.t]=true;
                    }
                }else if(d[j.t]==d[t]+1){
                    pre[j.t].add(t);
                    p[j.t]+=p[t];
                }
            }
        }
        while(stack.size()>1){
            int t=stack.pop();
            for(Integer j:pre[t]){
                if(j!=s){
                    delta[j]+=(1D+delta[t])*p[j]/p[t];
                }
            }
        }
        p[s]=0;
        for(int i=0;i<G.n;i++){
            bc[i]+=delta[i];
        }
    }


    void bfs(int s){
        Queue<Integer>q=new LinkedList<>();
        int[]d=new int[G.n];
        d[s]=0;
        q.add(s);
        v[s]=true;
        vis=new LinkedList<>();
        vis.add(s);
        int count=0;
        while(!q.isEmpty()){
            int t=q.poll();
            if(query_hop(s,t)<=d[t]){
                //System.out.println("Cut!!!");
                continue;
            }
            hub_hop.get(t).add(Pair.of(s,d[t]));
            num++;
            count++;
            for(Hop j:G.edges.get(t)){
                if(!v[j.t]){
                    d[j.t]=d[t]+1;
                    q.add(j.t);
                    v[j.t]=true;
                    vis.add(j.t);
                }
            }
        }
        for(Integer i:vis){
            v[i]=false;
        }
        Debug.print(cnt_i+"th node     "+"Add Hub_hop "+ count+".  Total: "+num);
    }

    void solve_hop() {
        num=0;
        List<Integer> enumeration=new ArrayList<>();
        for(int i=0;i<G.n;i++){
            enumeration.add(i);
        }
        enumeration.sort(Comparator.comparing(o->-G.edges.get(o).size()));
        /*
        if(Config.database.startsWith("dbpedia")) {
            bc = new double[G.n];
            for (int i = 0; i < G.n; i++) {
                Debug.print("BC hop node: " + i);
                cal_bc_hop(enumeration.get(i));
            }
            enumeration.sort(Comparator.comparing(o -> -bc[o]));
        }

         */




        rank=new int[G.n];
        hub_hop=new ArrayList<>();
        d=new double[G.n];
        Arrays.fill(d,inf);
        v=new boolean[G.n];
        for(int i=0;i<G.n;i++){
            rank[enumeration.get(i)]=i;
            hub_hop.add(new ArrayList<>());
        }
        cnt_i=0;
        for(Integer i:enumeration){
            cnt_i++;
            bfs(i);
        }

        try {
            connect();
            System.out.println("Writing Hub hop...");
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(
                    "create table hub_hop(" +
                            "    u int," +
                            "    v int," +
                            "    w int" +
                            ")" +
                            "engine="+ENGINE);
            stmt.close();
            conn.setAutoCommit(false);
            SQL_batch batch=new SQL_batch(conn,"insert into hub_hop values (?,?,?)");
            long start=System.currentTimeMillis();
            for (int i = 0; i < G.n; i++) {
                hub_hop.get(i).sort(Comparator.comparing(Pair::getFirst));
                for (Pair<Integer,Integer>j : hub_hop.get(i)) {
                    batch.add(i, j.getFirst(), j.getSecond());
                }
            }
            batch.close();
            long end=System.currentTimeMillis();
            System.out.println("Time hop: "+ (end-start));
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hub_hop=null;
    }
}
