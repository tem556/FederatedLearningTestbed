package com.bnnthang.fltestbed.Server;

import com.bnnthang.fltestbed.Server.AggregationStrategies.NewFedAvg;
import com.bnnthang.fltestbed.models.ServerParameters;
import com.bnnthang.fltestbed.models.TrainingConfiguration;
import com.bnnthang.fltestbed.servers.BaseServer;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            ML.trainAndEval();
        } else {
//            System.out.println("Hello World!");
//            FederatedLearningServer server = new FederatedLearningServerImpl();
//            server.startServer();
            NewFedAvg aggStrategy = new NewFedAvg();
            TrainingConfiguration trainConf = new TrainingConfiguration(3, 3, 5000, aggStrategy);
            ServerOperations servOps = new ServerOperations();
            ServerParameters serverParameters = new ServerParameters(4602, trainConf, servOps);
            BaseServer server = new BaseServer(serverParameters);
            server.serve();
        }
    }
}
