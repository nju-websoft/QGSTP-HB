package driver.work;

import driver.RunBase;
import mytools.Config;
import mytools.Info;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import static graphtheory.Structure.alpha;

public class Run extends RunBase {
    public static void process() {
        int count= Config.query_num;
        String resultfile=Config.result_file;
        List<Double> alpha_array=Config.alpha_array;

        try {
            WorkerBase work=null;
            if(Config.worker==1){
                work = new Worker(Config.database);
            }else if(Config.worker==2){
                work = new Worker2(Config.database);
            }else if(Config.worker==3){
                work = new Worker3(Config.database);
            }
            work.read_data();
            // Don't use PrintWrite here, because it will lose all the data if the process is interrupted.
            PrintStream out=new PrintStream(resultfile);
            out.println("----------------------------------------");
            Info info=new Info();
            if(Config.record_each_query) {
                info.init();
            }
            for (Algorithm_type algorithm_type:Algorithm_type.values()){
                int id=algorithm_type.ordinal();
                if (! Config.algorithm[id]){
                    continue;
                }
                work.reset_algo(algorithm_type);
                info.algorithm=algorithm_type.toString();

                for (Double enum_alpha:alpha_array){
                    work.reset_alpha(enum_alpha);
                    info.alpha=enum_alpha;

                    double time = 0;
                    for (int i=0;i<count;i++){
                        System.out.println("Expr "+i+":");
                        work.gen_keyword(i);
                        info.data_id=i;
                        info.g=work.Q.g;

                        RetInfo ret_info=work.run_algorithm();
                        time+=ret_info.time;
                        if (Config.record_each_query){
                            info.runtime=ret_info.time;
                            info.cost=ret_info.ans.cost;
                            info.anstree=new LinkedList<>();
                            info.anstree.addAll(ret_info.ans.map.keySet());
                            info.add();
                        }
                    }

                    out.println("alpha=" + alpha);
                    out.println("algorithm: "+ algorithm_type);
                    time/=count;
                    out.println("Time: " + time + " s");
                    out.println("----------------------------------------");
                }
            }
            out.close();
            work.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            ArgumentParser parser= ArgumentParsers.newFor("Run").build();
            parser.addArgument("-c","--config").setDefault("config.properties").help("config file");
            Namespace ns=null;
            try{
                ns=parser.parseArgs(args);
            } catch (ArgumentParserException e) {
                parser.handleError(e);
                System.exit(1);
            }
            Config.config_filename=ns.get("config");
            Config.init();
            process();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
