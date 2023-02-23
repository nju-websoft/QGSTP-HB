package driver.data;

import mytools.SQL_batch;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

/**
 * Read DBpedia into database
 * Here we need extra tables, maybe it will be combined in DBbuilder
 *      types:
 *          format: (id - name)
 *          content: map the typename to its id.
 *
 *      nodetype:
 *          format: (node - type)
 *          content: store the type of nodes
 *
 *      labels_list:
 *          format: (id - label)
 *          content: store the label of nodes
 *
 *      queries:
 *          format: (query - keyword)
 *          content: store the keywords of a query
 *
 * @Author qkoqhh
 * @Date 2020-11-4
 */
public class ReadDBpedia extends ReadMondial{
    /**
     * Create the experiment database, and initialize the tables: nodes, edges, keyword, keymap
     * The connection to database is hold on, remember to close it
     * @param dbname the name of database
     */
    SQL_batch types,nodetype,labels;
    ReadDBpedia(String dbname) {
        super(dbname);
        java.sql.Statement stmt=null;
        try{
            stmt=conn.createStatement();
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
            stmt.executeUpdate(
                    "create table labels(" +
                    "    id int," +
                    "    label varchar(1024)" +
                    ")" +
                    "engine="+ENGINE
            );


            types=new SQL_batch(conn,"insert into types values (?,?)");
            nodetype=new SQL_batch(conn,"insert into nodetype values (?,?)");
            labels=new SQL_batch(conn,"insert into labels_list values (?,?)");


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally{
            try{
                if(stmt!=null) {
                    stmt.close();
                }
            }catch(SQLException se2){
            }
        }
    }

    Map<String,Integer> typelist=new HashMap<>();


    int gettype(String type) throws Exception {
        if(!typelist.containsKey(type)){
            types.add(typelist.size(),type);
            typelist.put(type,typelist.size());
        }
        return typelist.get(type);
    }




    @Override
    void read_graph(String filename) throws Exception {
        System.out.println("Reading graph from file...");
        Model model= ModelFactory.createDefaultModel();
        model.read(filename);
        StmtIterator iter = model.listStatements();
        while(iter.hasNext()){
            Statement stmt=iter.next();
            int x=getnode(stmt.getSubject().toString());
            int y=getnode(stmt.getObject().toString());
            edges.add(x,y);
            jgraph.addEdge(x,y);
        }
        edges.close();
        model.close();
    }

    void read_type(String filename) throws Exception {
        System.out.println("Reading type from file...");
        Model model= ModelFactory.createDefaultModel();
        model.read(filename);
        StmtIterator iter = model.listStatements();
        while(iter.hasNext()){
            Statement stmt=iter.next();
            if(!nodemap.containsKey(stmt.getSubject().toString())){
                continue;
            }
            int x=nodemap.get(stmt.getSubject().toString());
            int y=gettype(stmt.getObject().toString());
            nodetype.add(x,y);
        }
        typelist = null;
        types.close();
        model.close();
    }



    void read_label(String filename) throws Exception {
        System.out.println("Reading label from file...");
        Model model= ModelFactory.createDefaultModel();
        model.read(filename);
        StmtIterator iter = model.listStatements();
        while(iter.hasNext()){
            Statement stmt=iter.next();
            if(!nodemap.containsKey(stmt.getSubject().toString())){
                continue;
            }
            int x=nodemap.get(stmt.getSubject().toString());
            String y=stmt.getObject().asLiteral().getString();
            labels_list.get(x).add(y);
            labels.add(x,y);
        }
        labels.close();
        model.close();
    }
    void read_query(String filename) throws Exception {
        System.out.println("Reading query from file...");
        Scanner in=new Scanner(new File(filename));

        for(int query_num=0,keyword_num=0;in.hasNext();query_num++){
            String line=in.nextLine();
            line=line.substring(line.indexOf('\t')+1).trim().replace(',',' ');
            String[]keywords=line.split(" ");
            for(int i=0;i<keywords.length;i++) {
                queries.add(query_num, keyword_num++);
            }
            read_keyword(keywords);
        }
        queries.close();
        keymap.close();
        keyword.close();
    }





}

/* ------------------------------------------------------------------------------ */
/*
    void add_type(int id,String typename) throws SQLException {
        types.setInt(1,id);
        types.setString(2,typename);
        types.addBatch();
        types_cnt++;
        if(types_cnt%batch_size==0){
            System.out.println("Add type "+types_cnt);
            types.executeBatch();
            conn.commit();
            types.clearBatch();
        }
    }

    void add_nodetype(int nodeid,int typeid) throws SQLException {
        nodetype.setInt(1,nodeid);
        nodetype.setInt(2,typeid);
        nodetype.addBatch();
        nodetype_cnt++;
        if(nodetype_cnt%batch_size==0){
            System.out.println("Add nodetype "+nodetype_cnt);
            nodetype.executeBatch();
            conn.commit();;
            nodetype.clearBatch();
        }
    }

    void add_query(int query,int keyword) throws SQLException {
        queries.setInt(1,query);
        queries.setInt(2,keyword);
        queries.addBatch();
        queries_cnt++;
        if(queries_cnt%batch_size==0){
            System.out.println("Add nodetype "+nodetype_cnt);
            queries.executeBatch();
            conn.commit();
            queries.clearBatch();
        }
    }

    void add_nodelabel(int nodeid,String label) throws SQLException {
        labels_list.setInt(1,nodeid);
        labels_list.setString(2,label);
        labels_list.addBatch();
        labels_cnt++;
        if(labels_cnt%batch_size==0){
            System.out.println("Add label "+labels_cnt);
            labels_list.executeBatch();
            conn.commit();
            labels_list.clearBatch();
        }
    }

 */

