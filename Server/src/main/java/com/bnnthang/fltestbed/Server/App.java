package com.bnnthang.fltestbed.Server;

import com.bnnthang.fltestbed.Server.AggregationStrategies.NewFedAvg;
import com.bnnthang.fltestbed.models.ServerParameters;
import com.bnnthang.fltestbed.models.TrainingConfiguration;
import com.bnnthang.fltestbed.servers.BaseServer;
import org.opencv.core.Core;

import java.io.IOException;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class App {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws Exception {
//        String path = App.class.getClassLoader().getResource("cifar-10/test_batch.bin").getFile();
//        System.out.println(path);
        if (args.length > 0) {
            ML.trainAndEval();
        } else {
            // load properties
            Properties conf = new Properties();
            conf.load(App.class.getClassLoader().getResourceAsStream("config.properties"));
            String workDir = conf.getProperty("workDir");
            String modelFile = conf.getProperty("modelFilename");
            int minClients = Integer.parseInt(conf.getProperty("minClients"));
            int port = Integer.parseInt(conf.getProperty("port"));
            int trainingRounds = Integer.parseInt(conf.getProperty("trainingRounds"));
            int pollInterval = Integer.parseInt(conf.getProperty("pollInterval"));

            NewFedAvg aggStrategy = new NewFedAvg();
            TrainingConfiguration trainConf = new TrainingConfiguration(minClients, trainingRounds, pollInterval, aggStrategy);
            ServerOperations servOps = new ServerOperations(workDir, modelFile);
            ServerParameters serverParameters = new ServerParameters(port, trainConf, servOps);
            BaseServer server = new BaseServer(serverParameters);
            System.out.println("serving...");
            server.serve();
        }
    }
}
