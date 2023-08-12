package driver.data;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.lang.Math.*;

/**
 * Used to read mondial, probably some part of the code can be used to read other form of KG.
 * The query file is written in file already.
 */
public class ReadMondial extends DBbuilder{

    /**
     * Create the experiment database, and initialize the tables: nodes, edges, keyword, keymap
     * The connection to database is hold on, remember to close it
     *
     * @param dbname the name of database
     */
    ReadMondial(String dbname) {
        super(dbname);
    }
    Map<String,Integer>nodemap=new HashMap<>();
    List<String>nodename=new ArrayList<>();
    List<List<String>> labels_list =new ArrayList<>();
    org.jgrapht.Graph<Integer, DefaultEdge>jgraph=new DefaultDirectedGraph<>(DefaultEdge.class);

    int getnode(String s){
        if(!nodemap.containsKey(s)){
            if(nodemap.size()==5792781){
                System.err.println(s);
            }
            jgraph.addVertex(nodemap.size());
            nodemap.put(s,nodemap.size());
            nodename.add(s);
            labels_list.add(new ArrayList<>());
        }
        return nodemap.get(s);
    }

    void read_graph(String filename) throws Exception {
        System.out.println("Reading graph...");
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

            if(pred.contains("name")){
                if (stmt.getObject().isLiteral()) {
                    labels_list.get(x).add(stmt.getObject().toString());
                    continue;
                }
            }

            /*
            if ("type".equals(pred)){
                continue;
            }

             */

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
        model.close();

    }

    protected void set_vertex_weight() throws Exception {
        System.out.println("Setting vertex weight...");
        int n=nodemap.size();
        PageRank<Integer,DefaultEdge> pageRank=new PageRank<>(jgraph);
        double min_pagerank=Double.MAX_VALUE;
        for(int i=0;i<n;i++){
            min_pagerank=min(min_pagerank,pageRank.getVertexScore(i));
        }
        for(int i=0;i<n;i++){
            double t=log10(pageRank.getVertexScore(i))-log10(min_pagerank);
            t=1-1/(1+exp(-t));
            nodes.add(i,nodename.get(i),t);
        }
        jgraph=null;
        nodes.close();
    }


    Path path;
    Directory dir;
    Analyzer analyzer;
    protected void build_lucene() throws IOException {
        System.out.println("Building Lucene....");
        int n=nodemap.size();
        assert n>0;

        path= Files.createTempDirectory("indexfile");
        dir= FSDirectory.open(path);
        analyzer=new StandardAnalyzer();

        IndexWriterConfig config=new IndexWriterConfig(analyzer);
        IndexWriter iwriter=new IndexWriter(dir,config);
        for(int i=0;i<n;i++){
            for(String label: labels_list.get(i)){
                Document doc=new Document();
                doc.add(new Field("id",Integer.toString(i), TextField.TYPE_STORED));
                doc.add(new Field("label",label, TextField.TYPE_STORED));
                iwriter.addDocument(doc);
            }
        }
        iwriter.close();
        labels_list.clear();
    }


    /**
     * Read keywords from a query
     * Attention to close the SQL_batch keymap
     * @param keywords the keywords of queries
     */
    int keyword_num;
    protected void read_keyword(String[]keywords) throws Exception {
        int g=keywords.length;
        IndexReader ireader= DirectoryReader.open(dir);
        IndexSearcher isearcher=new IndexSearcher(ireader);
        QueryParser parser=new QueryParser("label",analyzer);
        for(int i=0;i<g;i++)if(!keywords[i].isEmpty()){
            keyword.add(keyword_num, keywords[i]);
            Query query = parser.parse(keywords[i]);
            ScoreDoc[] hits=isearcher.search(query,Integer.MAX_VALUE).scoreDocs;
            if (hits.length==0){
                System.err.println("Not Match: "+keywords[i]);
            }
            Set<Integer>nodeset=new HashSet<>();
            for(int j=0;j< hits.length;j++){
                Document doc=isearcher.doc(hits[j].doc);
                int node=Integer.parseInt(doc.get("id"));
                if(!nodeset.contains(node)) {
                    keymap.add(keyword_num, node);
                    nodeset.add(node);
                }
            }
            keyword_num++;
        }
        ireader.close();
    }
    void read_query(String filename) throws Exception {
        System.out.println("Reading query from file...");
        Scanner in=new Scanner(new File(filename));

        for(int query_num=0,keyword_num=0;in.hasNext();query_num++){
            String line=in.nextLine();
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