package driver.data;

import graphtheory.Structure;
import mytools.Config;
import mytools.Validator;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import driver.RunBase;

import java.sql.SQLException;

/**
 * A runner to process some insignificant work
 */
public class Run1 extends RunBase {
    static final String defalut_progress="ReadYago";
    static String progress;


    public static void process() throws Exception {
        System.out.println("Progress: "+progress);
        switch (progress){
            case "GenerateHubLabel": generateHubLabel();break;
            case "TestHub": testHub();break;
            case "GenFixQueryByRandom": genFixQueryByRandom();break;
            case "GenerateSubDBpedia": generateSubDBPedia();break;
            case "ReadOpenCyc": readOpenCyc();break;
            case "ReadYago": readYago();break;
            case "ReadLUBM": readLUBM();break;
            case "ReadDBpedia": readDBpedia();break;
            case "ReadMondial": readMondial();break;
            case "ReadLinkedmdb": readLinkedmdb();break;
        }
    }


    static void generateHubLabel(){
        GenerateHubLabel generator=new GenerateHubLabel(Config.database);
//        generator.solve_sal();
//        generator.solve_sd();
        generator.solve_hop();
        for (Double a:Config.alpha_array) {
            generator=new GenerateHubLabel(Config.database);
            Structure.alpha=a;
            Structure.beta=1-a;
            generator.solve_mix();
        }
    }

    static void testHub() throws Exception {
        TestHub here = new TestHub(Config.database);
        here.source_sample_hop(100, 100000);
        here.source_sample_mix(100, 100000);
    }


    public static void main(String[] args) {
        try {
            ArgumentParser parser = ArgumentParsers.newFor("Run1").build();
            parser.addArgument("-c", "--config").setDefault("config.properties").help("config file");
            parser.addArgument("-p","--progress").setDefault(defalut_progress).help("choose progress in Run1");
            Namespace ns = null;
            try {
                ns = parser.parseArgs(args);
            } catch (ArgumentParserException e) {
                parser.handleError(e);
                System.exit(1);
            }
            Config.config_filename = ns.get("config");
            progress = ns.get("progress");
            Config.init();
            if (Config.alpha_array.size() == 1) {
                Structure.alpha = Config.alpha_array.get(0);
                Structure.beta = 1 - Structure.alpha;
            }

            process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Generate Sub DBPedia from dbpedia, and generate hub label
     */
    static void generateSubDBPedia() throws Exception {
        GenerateSubDBPedia here=new GenerateSubDBPedia(Config.database);
        here.connect();
        here.read_graph();
        //Validator.comp(here.G);
        //here.generate(1000);
        here.generate(50000);
        //here.generate(500000);
//        here.generate(6000000);
    }

    static void genFixQueryByRandom() throws Exception {
        GenFixQueryByRandom here = new GenFixQueryByRandom(Config.database);
        here.read_graph();
        here.gen();
    }


    static void readLUBM() throws Exception {
        String pwd="D:\\work\\resources\\";
        String graph_name="lubm_250u";
        ReadLUBM here=new ReadLUBM(graph_name);
        System.out.println("Reading graph...");
        here.read_graph(pwd+graph_name+".nt");

        here.set_vertex_weight();

        System.out.println("Write in...");
        here.close();
    }

    static void readDBpedia() throws Exception {
        ReadDBpedia here = new ReadDBpedia("dbpedia");
        final String pwd = "D:\\work\\resources\\dbp\\";
        here.read_graph(pwd + "mappingbased_objects_en.ttl");
        here.set_vertex_weight();
        here.read_type(pwd + "instance_types_transitive_en.ttl");
        here.read_label(pwd + "labels_en.ttl");
        here.build_lucene();
        here.read_query(pwd + "dbpedia.queries-v2_stopped.txt");
        here.close();
    }


    static void readMondial() throws Exception {
        String pwd = "D:\\work\\resources\\";
        ReadMondial here = new ReadMondial("mondial");
        here.read_graph(pwd + "mondial-mod.nt");
        here.set_vertex_weight();
        here.build_lucene();
        System.out.println("Reading keywords...");
        here.read_query(pwd + "query\\Mondial.txt");
    }

    static void readOpenCyc() throws Exception {
        String pwd="D:\\work\\resources\\";
        ReadOpenCyc here=new ReadOpenCyc("opencyc");
        here.read_graph(pwd+"open-cyc-mod.rdf");
        here.set_vertex_weight();
    }

    static void readYago() throws Exception {
        String pwd="D:\\work\\resources\\";
        ReadYago here=new ReadYago("yago");
        here.read_graph(pwd+"yago-1.0.0-turtle.ttl");
        here.set_vertex_weight();
        here.build_lucene();
        here.read_query(pwd+"query\\yago.txt");
    }


    static void readLinkedmdb() throws Exception {
        String pwd = "D:\\work\\resources\\linkedmdb\\";
        ReadLinkedmdb here = new ReadLinkedmdb("LMDB");
        here.read_graph(pwd + "linkedmdb-latest-dump1.nt");
        here.set_vertex_weight();
        here.build_lucene();
        here.read_query(pwd + "query.txt");
    }

}
