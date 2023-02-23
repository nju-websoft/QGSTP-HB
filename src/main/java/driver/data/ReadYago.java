package driver.data;

import mytools.SQL_batch;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class ReadYago extends ReadLUBM{

    /**
     * Create the experiment database, and initialize the tables: nodes, edges, keyword, keymap
     * The connection to database is hold on, remember to close it
     *
     * @param dbname the name of database
     */
    ReadYago(String dbname) {
        super(dbname);
        try{
            Statement stmt=conn.createStatement();
            stmt.executeUpdate(
                    "create table types(" +
                            "    id int," +
                            "    name varchar(1024)" +
                            ")" +
                            "engine="+ ENGINE);
            stmt.executeUpdate(
                    "create table nodetype(" +
                            "    node int," +
                            "    type int" +
                            ")" +
                            "engine="+ENGINE
            );
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        types=new SQL_batch(conn,"insert into types values (?,?)");
        nodetype=new SQL_batch(conn,"insert into nodetype values (?,?)");
    }
    SQL_batch nodetype,types;
    Map<String,Integer> typelist =new HashMap<>(), keywordlist=new HashMap<>();
    List<Set<Integer>> keymaplist=new ArrayList<>();


    int getnode(String s){
        if(!nodemap.containsKey(s)){
            directtype.add(new ArrayList<>());
            jgraph.addVertex(nodemap.size());
            nodemap.put(s,nodemap.size());
            nodename.add(s);
            labels_list.add(new ArrayList<>());
        }
        return nodemap.get(s);
    }

    int gettype(String type) throws Exception {
        if(!typelist.containsKey(type)){
            types.add(typelist.size(),type);
            typelist.put(type,typelist.size());
            typelink.add(new ArrayList<>());
        }
        return typelist.get(type);
    }

    List<List<Integer>>typelink=new ArrayList<>();
    List<List<Integer>>directtype=new ArrayList<>();

    @Override
    void read_graph(String filename) throws Exception {
        Model model= ModelFactory.createDefaultModel();
        model.read(filename);

        StmtIterator iter = model.listStatements();
        while(iter.hasNext()){
            org.apache.jena.rdf.model.Statement stmt=iter.next();
            String pred = stmt.getPredicate().getLocalName();

            if(!stmt.getSubject().isResource()){
                continue;
            }
            if("subClassOf".equals(pred)){
                int u=gettype(stmt.getSubject().toString()),v=gettype(stmt.getObject().toString());
                typelink.get(u).add(v);
//                continue;
            }
            int x=getnode(stmt.getSubject().toString());
            if ("type".equals(pred)){
                String type=stmt.getObject().toString();
                directtype.get(x).add(gettype(type));
//                continue;
            }
            if (pred.contains("Name")||pred.contains("name")||pred.contains("label")){
                if(stmt.getObject().isLiteral()){
                    labels_list.get(x).add(stmt.getObject().toString());
                    continue;
                    /*
                    String[] keywords=stmt.getObject().asLiteral().getString()
                            .replaceAll("[@/<>(),:&;=?!.#$\"\'` ]"," ")
                            .replace("\\"," ")
                            .split("\\s+");
                    for (String word:keywords){
                        if (word.length()<=1)continue;
                        word=word.toLowerCase(Locale.ROOT);
                        if (!keywordlist.containsKey(word)){
                            keyword.add(keywordlist.size(),word);
                            keywordlist.put(word,keywordlist.size());
                            keymaplist.add(new HashSet<>());
                        }
                        keymaplist.get(keywordlist.get(word)).add(x);
                    }
                     */
                }
            }

            if(!stmt.getObject().isResource()){
                continue;
            }
            int y=getnode(stmt.getObject().toString());

            if(x!=y){
                edges.add(x,y);
            }
            jgraph.addEdge(x,y);
        }
        edges.close();
        types.close();
        model.close();



        int m=directtype.size();
        for (int i=0;i<m;i++){
            Queue<Integer>q=new LinkedList<>();
            Set<Integer>s=new HashSet<>();
            for (Integer j:directtype.get(i)){
                q.add(j);
                s.add(j);
            }
            while(!q.isEmpty()){
                int t=q.poll();
                for(Integer j:typelink.get(t)){
                    if(!s.contains(j)){
                        q.add(j);
                        s.add(j);
                    }
                }
            }
            for (Integer j:s){
                nodetype.add(i,j);
            }
        }
        nodetype.close();

        /*
        keyword.close();
        int n=keymaplist.size();
        for (int i=0;i<n;i++){
            for (Integer j:keymaplist.get(i)){
                keymap.add(i,j);
            }
        }
        keymap.close();
         */
    }
    /*
    void read_query(String filename){
        System.out.println("Reading query...");
        try {
            BufferedReader in=new BufferedReader(new FileReader(filename));
            for(int i=0;true;i++){
                String line=in.readLine();
                if(line==null)break;
                String[] keywords=line.split(" ");
                for(String keyword : keywords){
                    if (keywordlist.containsKey(keyword)){
                        int key=keywordlist.get(keyword);
                        queries.add(i,key);
                    }else{
//                        System.err.println(keyword);
//                        System.err.println("Not Match");
                        throw new Exception("Not Match: "+keyword);
                    }
                }
            }
            queries.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     */
}
