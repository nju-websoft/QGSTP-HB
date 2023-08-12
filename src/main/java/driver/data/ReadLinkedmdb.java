package driver.data;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * The graph file has been prepared previously.
 */
public class ReadLinkedmdb extends ReadMondial{
    /**
     * Create the experiment database, and initialize the tables: nodes, edges, keyword, keymap
     * The connection to database is hold on, remember to close it
     *
     * @param dbname the name of database
     */
    ReadLinkedmdb(String dbname) {
        super(dbname);
    }

    void read_graph(String filename) throws Exception {
        System.out.println("Reading graph...");
        Model model= ModelFactory.createDefaultModel();
        model.read(filename);
        StmtIterator iter = model.listStatements();
        while(iter.hasNext()){
            Statement stmt=iter.next();
            String pred=stmt.getPredicate().getLocalName();
            int x=getnode(stmt.getSubject().toString());
            if("label".equals(pred)){
                labels_list.get(x).add(stmt.getObject().asLiteral().getString());
                continue;
            }
            /*
            if("type".equals(pred)){
                continue;
            }
             */
            if(!stmt.getObject().isResource()){
                continue;
            }
            int y=getnode(stmt.getObject().asResource().toString());
            if(x!=y) {
                edges.add(x, y);
            }
            jgraph.addEdge(x,y);
        }
        model.close();
        edges.close();
    }

}
