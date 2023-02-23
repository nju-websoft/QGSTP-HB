package driver.data;

import mytools.Config;
import mytools.SQL_batch;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class ReadOpenCyc extends ReadYago{
    /**
     * Create the experiment database, and initialize the tables: nodes, edges, keyword, keymap
     * The connection to database is hold on, remember to close it
     *
     * @param dbname the name of database
     */
    ReadOpenCyc(String dbname) {
        super(dbname);
    }
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
            int x=getnode(stmt.getSubject().toString());
            if ("type".equals(pred)){
                String type=stmt.getObject().toString();
                if (!typelist.containsKey(type)){
                    types.add(typelist.size(),type);
                    typelist.put(type,typelist.size());
                }
                nodetype.add(x,typelist.get(type));
//                continue;
            }
            if (pred.contains("prettyString")||pred.contains("comment")){
                if(stmt.getObject().isLiteral()){
//                    labels_list.get(x).add(stmt.getObject().toString());
//                    continue;
                    String[] keywords=stmt.getObject().asLiteral().getString()
                            .replaceAll("[*+/<>(),:&;=?!.#$\"'` ]"," ")
                            .replace("\\"," ")
                            .split("\\s+");
                    for (String word:keywords){
                        if (word.length()<=1)continue;
                        if (!keywordlist.containsKey(word)){
                            keyword.add(keywordlist.size(),word);
                            keywordlist.put(word,keywordlist.size());
                            keymaplist.add(new HashSet<>());
                        }
                        keymaplist.get(keywordlist.get(word)).add(x);
                    }
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
        nodetype.close();
        keyword.close();
        model.close();

        int n=keymaplist.size();
        for (int i=0;i<n;i++){
            for (Integer j:keymaplist.get(i)){
                keymap.add(i,j);
            }
        }
        keymap.close();

        Random rand=new Random();
        for (int i=0;i<50;i++){
            int m=rand.nextInt(6)+1;
            for (int j=0;j<m;j++){
                int k=rand.nextInt(keywordlist.size());
                queries.add(i,k);
            }
        }
        queries.close();
    }

}
