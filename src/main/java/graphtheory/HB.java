package graphtheory;

import graphtheory.hub.HubHopIter;
import graphtheory.hub.HubMixIter;
import mytools.*;
import org.jgrapht.alg.util.Pair;

import java.util.*;

import static graphtheory.Structure.*;
import static java.lang.Math.*;

public class HB extends Algorithm {
    public HB(Graph G) {
        super(G);
        init(G.n, 10);
    }

    void init(int n, int m) {
        a = new double[n];
        dis = new double[n];
        Arrays.fill(dis, inf);
        length = new int[n];


        // The max hop of stratified the shortest path in practice (maybe)
        final int bound = 80;

        d = new double[bound][n];
        for (int i = 0; i < bound; i++) {
            Arrays.fill(d[i], inf);
        }
        pd = new int[bound][n];

        g = new double[m][bound];
        for (int i = 0; i < m; i++) {
            Arrays.fill(g[i], inf);
        }
        pg = new int[m][bound];

        pre = new int[m + 1][bound];


        upper_d = new double[n];
        Arrays.fill(upper_d, inf);


        v = new boolean[n];
        vis = new boolean[n];
        visitor = new LinkedList<>();

        if (!Config.woPR) {
            hub_mix=new DynamicDoubleArray[m];
            for (int i=0;i<m;i++){
                hub_mix[i]=new DynamicDoubleArray(n);
            }
        }
        if (!Config.woPR || !Config.woPI) {
            lb_hop = new int[n];
            hop = new int[n];
            Arrays.fill(hop, MAXINT);
        }
        if (!Config.woPI) {
            hop_sum = new int[n];
            pre_hop = new List[n];
            leaf = new ArrayList<>(m);
            for (int i = 0; i < m; i++) {
                leaf.add(null);
            }
        }
        Rm = new int[n];
    }

    @Override
    public void set_query(Query Q) {
        this.Q = Q;
        len = new int[Q.g];
        past_dis = new double[Q.g];
        lp = new double[Q.g];
    }


    int[] lb_hop;
    DynamicDoubleArray[] hub_mix;
    int[] hop;
    int[] hop_sum;
    int[] Rm;

    void hub_init(int kmin) {
        Set<Integer> s = new HashSet<>();
        for (Integer j : Q.keywords.get(kmin)) {
            if (!Config.woPR) {
                lb_hop[j] = 1;
            }
            Rm[j]=0;
            if (!Config.woPI) {
                for (HubHopIter k = new HubHopIter(G.hub_hop, j); k.hasNext(); k.next()) {
                    if (s.contains(k.v)) {
                        if (k.w < hop_sum[k.v]) {
                            hop_sum[k.v] = k.w;
                        }
                    } else {
                        s.add(k.v);
                        hop_sum[k.v] = k.w;
                    }
                }
            }
        }
        simple_candidate = new LinkedList<>(s);


        for (int i = 0; i < Q.g; i++) {
            if (!Config.woPR) {
                for (Integer j : Q.keywords.get(i)) {
                    for (HubMixIter k = new HubMixIter(G.hub_mix, j); k.hasNext(); k.next()) {
                        hub_mix[i].put(k.v,k.w);
                    }
                }
            }
            if (i == kmin) {
                continue;
            }

            if(!Config.woPR || !Config.woPI) {
                for (Integer j : Q.keywords.get(i)) {
                    for (HubHopIter k = new HubHopIter(G.hub_hop, j); k.hasNext(); k.next()) {
                        if (hop[k.v] == MAXINT) {
                            visitor.add(k.v);
                        }
                        if (k.w < hop[k.v]) {
                            hop[k.v] = k.w;
                        }
                    }
                }
                if (!Config.woPR) {
                    for (Integer j : Q.keywords.get(kmin)) {
                        int ans = MAXINT;
                        for (HubHopIter k = new HubHopIter(G.hub_hop, j); k.hasNext(); k.next()) {
                            if (hop[k.v] + k.w < ans) {
                                ans = hop[k.v] + k.w;
                            }
                        }
                        lb_hop[j] += ans;
                        Rm[j] = max(Rm[j], ans);
                    }
                }
                if (!Config.woPI) {
                    List<Integer> nxt = new LinkedList<>();
                    for (Integer j : simple_candidate) {
                        if (hop[j] < MAXINT) {
                            nxt.add(j);
                            hop_sum[j] += hop[j];
                        }
                    }
                    simple_candidate = nxt;
                }
                for (Integer j : visitor) {
                    hop[j] = MAXINT;
                }
                visitor.clear();
            }
        }
    }

    double[]lp;
    double query_mix(int r) {
        Arrays.fill(lp, inf);
        for (HubMixIter k = new HubMixIter(G.hub_mix, r); k.hasNext(); k.next()) {
            for (int i = 0; i < Q.g; i++) {
                lp[i] = min(lp[i], hub_mix[i].get(k.v) + k.w - alpha*G.a[k.v]);
            }
        }
        for (int i = 0; i < Q.g; i++) {
            lp[i] -= alpha * G.a[r];
        }
        return Arrays.stream(lp).sum() + alpha * G.a[r];
    }


    int[] len;
    List<Integer>[] pre_hop;
    List<List<Integer>> leaf;

    void simple_bfs(final int r, final int[] d) {
        Queue<Integer> q = new LinkedList<>();
        q.add(r);
        d[r] = 0;
        visitor.add(r);
        pre_hop[r] = new LinkedList<>();
        v[r] = true;
        int cnt = Q.g;
        Arrays.fill(len, -1);
        for (Integer i : G.key.get(r)) {
            len[i] = 0;
            cnt--;
            leaf.set(i, new LinkedList<>());
            leaf.get(i).add(r);
        }
        while (!q.isEmpty()) {
            int t = q.poll();
            for (Hop j : G.edges.get(t)) {
                if (!v[j.t]) {
                    d[j.t] = d[t] + 1;
                    v[j.t] = true;
                    visitor.add(j.t);
                    if (cnt > 0) {
                        q.add(j.t);
                    }
                    pre_hop[j.t] = new LinkedList<>();
                    pre_hop[j.t].add(t);
                    for (Integer i : G.key.get(j.t)) {
                        if (len[i] == -1) {
                            len[i] = d[j.t];
                            leaf.set(i, new LinkedList<>());
                            leaf.get(i).add(j.t);
                            cnt--;
                            if (cnt == 0) {
                                q.removeIf(o -> (d[o] >= d[j.t]));
                            }
                        } else if (len[i] == d[j.t]) {
                            leaf.get(i).add(j.t);
                        }
                    }
                } else if (d[j.t] == d[t] + 1) {
                    pre_hop[j.t].add(t);
                }
            }
        }
    }

    Path get_simple_path(int r, int key, double[] d, int[] p) {
        List<List<Integer>> layer = new ArrayList<>(len[key] + 1);
        for (int i = 0; i <= len[key]; i++) {
            layer.add(new LinkedList<>());
        }
        for (Integer i : leaf.get(key)) {
            layer.get(0).add(i);
            d[i] = alpha * G.a[i] + beta * G.sd.cal(r, i);
            p[i] = -1;
        }
        for (int i = 1; i <= len[key]; i++) {
            for (Integer j : layer.get(i - 1)) {
                for (Integer k : pre_hop[j]) {
                    double new_dis = d[j] + alpha * G.a[k] + beta * G.sd.cal(r, k);
                    if (new_dis < d[k]) {
                        if (Double.compare(d[k], inf) == 0) {
                            layer.get(i).add(k);
                        }
                        p[k] = j;
                        d[k] = new_dis;
                    }
                }
            }
        }
        Path ret = new Path();
        ret.cost = d[r] - alpha * G.a[r] - beta * G.sd.cal(r, r);
        for (int x = r; x >= 0; x = p[x]) {
            ret.nodes.add(x);
        }

        for (int i = 0; i <= len[key]; i++) {
            for (Integer j : layer.get(i)) {
                d[j] = inf;
            }
        }
        return ret;
    }


    int ref_dfs(List<List<Integer>> edges, int r, int f, int[] count) {
        int cnt = 0;
        for (Integer j : edges.get(r)) {
            if (j != f) {
                cnt += ref_dfs(edges, j, r, count);
            }
        }
        if (cnt == 0) {
            cnt++;
        }
        count[r] = cnt;
        return cnt;
    }

    void refine(Tree tree) {
        if (Config.woPI) return;
        final int n = tree.map.size();
        final int[] node = new int[n];
        final List<List<Integer>> edges = new ArrayList<>(n);
        for (Map.Entry<Integer, Integer> r : tree.map.entrySet()) {
            node[r.getValue()] = r.getKey();
        }
        for (int i = 0; i < n; i++) {
            edges.add(new LinkedList<>());
        }
        for (Edge e : tree.edges) {
            edges.get(tree.map.get(e.s)).add(tree.map.get(e.t));
            edges.get(tree.map.get(e.t)).add(tree.map.get(e.s));
        }
        final double[][] sd = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                sd[i][j] = sd[j][i] = G.sd.cal(node[i], node[j]);
            }
        }
        final int[] count = new int[n];
        for (int r = 0; r < n; r++) {
            ref_dfs(edges, r, -1, count);
            count[r] = 1;
//            for (int R = 1; R < n; R++) {
                final int m = Arrays.stream(count).sum();
                double t = 0;
                for (int i = 0; i < n; i++) {
                    t += (alpha * G.a[node[i]] + beta * sd[r][i]) * count[i];
                }
                t = t * m;
                if (t < ansRPS.cost) {
                    ansRPS.cost = t;
                }
//            }
        }
    }

    List<Integer> simple_candidate;

    void simple_search(int r,int n){
        final int[] p = length, d = hop;
        simple_bfs(r, d);
        RPS rps = new RPS();
        rps.cost = alpha * G.a[r];
        for (int i = 0; i < Q.g; i++) {
            Path path = get_simple_path(r, i, dis, p);
            rps.paths.add(path);
            rps.cost += path.cost;
        }
        rps.cost = rps.cost * n;
        Tree tree = new Tree(G, rps);
        tree.cal_cost(G);
        if (ansRPS.cost > rps.cost) {
            Debug.print("RPS Used");
            ansRPS = rps;
        }
        if (ansTree.cost > tree.cost) {
            Debug.print("Tree Used");
            ansTree = tree;
//            refine(tree);
        }

        for (Integer i : visitor) {
            d[i] = MAXINT;
            v[i] = false;
        }
        visitor.clear();
    }

    void simple_tree() {
        Debug.print("Process SPT");
        Debug.start();
        List<Integer> nxt = new LinkedList<>();
        int cnt = MAXINT;
        for (Integer r : simple_candidate) {
            if (hop_sum[r] < cnt) {
                nxt.clear();
                nxt.add(r);
                cnt = hop_sum[r];
            } else if (hop_sum[r] == cnt) {
                nxt.add(r);
            }
        }
        simple_candidate = nxt;
        Debug.print("Simple Size: "+simple_candidate.size()+" ; Min Hop: "+cnt);


        for (Integer r : simple_candidate) {
            simple_search(r, hop_sum[r] + 1);
        }
        refine(ansTree);
        Debug.end("SPT Time");
    }

    void clear() {
        for (Integer i : visitor) {
            upper_d[i] = inf;
            dis[i] = inf;
            v[i] = false;
            vis[i] = false;
        }
        visitor.clear();
    }

    double[] dis, a;
    boolean[] v, vis;
    List<Integer> visitor = new LinkedList<>();


    int[] length;

    boolean upper_dij(int r) {
        final double upper_s = ansRPS.cost / lb_hop[r];
        int cnt = 0;
        PriorityQueue<Pair<Integer, Double>> q = new PriorityQueue<>(Comparator.comparing((Pair::getSecond)));
        a[r] = alpha * G.a[r];
        dis[r] = 0;
        length[r] = 0;
        double s = a[r];
        double bound = inf, second_bound = inf;
        if (!Config.woPP) {
            bound = upper_s - s - Arrays.stream(lp).sum() + Arrays.stream(lp).max().getAsDouble();
        }
        if (!Config.woPP){
            second_bound = (upper_s - s)/Q.g;
        }
        q.add(new Pair<>(r, dis[r]));
        vis[r] = true;
        visitor.add(r);
        while (!q.isEmpty()) {
            int t = q.poll().getFirst();
            if (v[t]) {
                continue;
            }
            v[t] = true;
            for (Integer k : G.key.get(t)) {
                if (len[k] == -1) {
                    len[k] = length[t];
                    s += dis[t];
                    cnt++;
                    if (!Config.woPP) {
                        past_dis[k] = dis[t];
                    }
                    if (!Config.woPP) {
                        lp[k] = 0;
                        bound = upper_s - s - Arrays.stream(lp).sum() + Arrays.stream(lp).max().getAsDouble();
                    }
                    if (upper_s <= s) {
                        return false;
                    }
                    if (cnt == Q.g) {
                        return true;
                    }
                    if (!Config.woPP){
                        second_bound = (upper_s -s )/(Q.g-cnt);
                    }
                }
            }

            if (!Config.woPP) {
                if (dis[t] >= bound) {
                    Debug.print("OPT6 CUT");
                    break;
                }
            }
            if (!Config.woPP){
                if (dis[t] >= second_bound){
                    Debug.print("OPT28 CUT");
                    break;
                }
            }


            for (Hop j : G.edges.get(t)) {
                if (!vis[j.t]) {
                    a[j.t] = alpha * G.a[j.t] + beta * G.sd.cal(r, j.t);
                    vis[j.t] = true;
                    visitor.add(j.t);
                }
                if (!v[j.t] && dis[j.t] > dis[t] + a[j.t]) {
                    dis[j.t] = dis[t] + a[j.t];
                    length[j.t] = length[t] + 1;
                    if (!Config.woPP && dis[j.t] >= bound) {
                        continue;
                    }
                    q.add(new Pair<>(j.t, dis[j.t]));
                }
            }
        }
        return false;
    }

    double[] past_dis;
    double[][] d;
    int[][] pd;
    double[] upper_d;

    void stratified_shortest_path(int r) {
        Set<Integer>[] q = new Set[]{new HashSet<>(), new HashSet<>()};
        int _t = 0;
        double upper_bound = !Config.woPP ? ansRPS.cost / lb_hop[r] - a[r] - Arrays.stream(past_dis).sum() + Arrays.stream(past_dis).max().getAsDouble() : MAXINT ;
        if(Config.woPR) {
            for (int k = 0; k < Q.g; k++) {
                if (len[k] == -1) len[k] = G.n - 1;
            }
        }
        final int max_length = Arrays.stream(len).max().getAsInt();
        q[_t].add(r);
        d[0][r] = 0;
        int i;
        for ( i=0 ; i < max_length && !q[_t].isEmpty(); i++) {
            if (!Config.woPP && Rm[r] < i) {
                upper_bound = ansRPS.cost / (lb_hop[r] - Rm[r] + i)
                        - a[r] - Arrays.stream(past_dis).sum() + Arrays.stream(past_dis).max().getAsDouble();
            }
            for (Integer j : q[_t]) {
                for (Hop k : G.edges.get(j)) {
                    if (!vis[k.t]) {
                        vis[k.t] = true;
                        visitor.add(k.t);
                        a[k.t] = alpha * G.a[k.t] + beta * G.sd.cal(r, k.t);
                    }
                    if (upper_d[k.t] <= d[i][j] + a[k.t]) {
                        continue;
                    }
                    d[i + 1][k.t] = d[i][j] + a[k.t];
                    upper_d[k.t] = d[i + 1][k.t];
                    if (!Config.woPP && d[i + 1][k.t] > upper_bound) {
                        continue;
                    }
                    pd[i + 1][k.t] = j;
                    q[_t ^ 1].add(k.t);
                }
                for (Integer k : G.key.get(j)) {
                    if (i <= len[k] && d[i][j] < g[k][i]) {
                        g[k][i] = d[i][j];
                        pg[k][i] = j;
                    }
                }
                d[i][j] = inf;
            }
            q[_t].clear();
            _t ^= 1;
        }

        for (Integer j : q[_t]) {
            for (Integer k : G.key.get(j)) {
                if (max_length<= len[k] && d[max_length][j] < g[k][max_length]) {
                    g[k][max_length] = d[max_length][j];
                    pg[k][max_length] = j;
                }
            }
            d[max_length][j] = inf;
        }
        q[_t].clear();

        if(Config.woPP) {
            for (int k = 0; k < Q.g; k++) {
                len[k] = min(len[k], i);
            }
        }

    }

    double[][] g;
    int[][] pg;

    double[][] pack;
    int[][] pre;

    void packsnap(int r) {
        final int n = Arrays.stream(len).sum();
        pack = new double[Q.g + 1][n + 1];
        for (int i = 0; i <= Q.g; i++) {
            Arrays.fill(pack[i], inf);
        }
        pack[0][0] = a[r];
        for (int i = 0; i < Q.g; i++) {
            for (int k = 0; k <= len[i]; k++) {
                if (g[i][k] < inf) {
                    for (int j = k; j <= n; j++) {
                        if (pack[i][j - k] + g[i][k] < pack[i + 1][j]) {
                            pack[i + 1][j] = pack[i][j - k] + g[i][k];
                            pre[i + 1][j] = k;
                        }
                    }
                    g[i][k] = inf;
                }
            }
        }
        int size = 0;
        for (int k = 0; k <= n; k++) {
            if (pack[Q.g][k] * (k + 1) < pack[Q.g][size] * (size + 1)) {
                size = k;
            }
        }
        if (pack[Q.g][size] * (size + 1) < ansRPS.cost) {
            Debug.print("RPS Used");
            RPS rps = new RPS();
            rps.cost = pack[Q.g][size] * (size + 1);
            for (int i = Q.g, j = size; i > 0; j -= pre[i][j], i--) {
                Path path = new Path();
                path.cost = g[i - 1][pre[i][j]];
                for (int k = pre[i][j], x = pg[i - 1][k]; k >= 0; x = pd[k][x], k--) {
                    path.nodes.add(x);
                }
                rps.paths.add(path);
            }
            ansRPS = rps;

            Tree tree = new Tree(G, rps);
            tree.cal_cost(G);
            if (tree.cost < ansTree.cost) {
                Debug.print("Tree Used!");
                ansTree = tree;
            }
        }


    }

    RPS ansRPS;
    Tree ansTree;

    @Override
    public Tree solve() {
        ansTree = new Tree();
        ansTree.cost = inf;
        ansRPS = new RPS();
        ansRPS.cost = inf;
        int kmin = rand.nextInt(Q.g);
        for (int i = 0; i < Q.g; i++) {
            if (Q.keywords.get(i).size() < Q.keywords.get(kmin).size()) {
                kmin = i;
            }
        }

        hub_init(kmin);
        if (!Config.woPI) {
            simple_tree();
        }


        if (!Config.woPR) {
            Q.keywords.get(kmin).sort(Comparator.comparing(o -> lb_hop[o]));
        }


        for (Integer r : Q.keywords.get(kmin)) {
            if (!Config.woPR) {
                final double lb = query_mix(r);
                if (Dcmp.ge(lb * lb_hop[r], ansRPS.cost)) {
                    Debug.print("OPT22 CUT");
                    continue;
                }
                if (!Config.woPI && Dcmp.ge(lb * (lb_hop[r] + 1), ansRPS.cost)) {
                    final double ans = ansTree.cost;
                    simple_search(r, lb_hop[r]);
                    if (ansTree.cost != ans) {
                        refine(ansTree);
                    }
                    Debug.print("OPT27 CUT");
                    continue;
                }
                Debug.print(r + " " + lb + " " + lb_hop[r] + " " + ansRPS.cost);
            }
            Debug.start();

            Arrays.fill(past_dis,0);
            Arrays.fill(len, -1);
            boolean ret_dij=!Config.woPP || !Config.woPR ? upper_dij(r) : true;
            Debug.end("Dij Time");
            Debug.print("Dij size : " + visitor.size());
            Debug.start();
            if (ret_dij) {
                stratified_shortest_path(r);
                packsnap(r);
                refine(ansTree);
                Debug.end("Execute Time");
            } else {
                Debug.print("Dij Cut");
            }
            clear();
            if (System.currentTimeMillis() - start > timeout) {
                return ansTree;
            }
        }

        if(!Config.woPR) {
            for (int i = 0; i < Q.g; i++) {
                hub_mix[i].clear();
            }
        }

        return ansTree;
    }
}
