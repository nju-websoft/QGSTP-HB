package driver.data;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;

import java.sql.SQLException;

public class ReadLUBM extends ReadMondial{
    /**
     * Create the experiment database, and initialize the tables: nodes, edges, keyword, keymap
     * The connection to database is hold on, remember to close it
     * @param dbname the name of database
     */
    ReadLUBM(String dbname) {
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

            if(!stmt.getSubject().isURIResource()){
                continue;
            }
            int x=getnode(stmt.getSubject().toString());
            /*
            if("type".equals(pred)){
                continue;
            }

             */

            if(!stmt.getObject().isURIResource()){
                continue;
            }
            int y=getnode(stmt.getObject().toString());

            if(x!=y){
                edges.add(x,y);
            }
            jgraph.addEdge(x,y);
        }
        edges.close();
        model.close();
    }

    public static void main(String[]args){
    }
}
