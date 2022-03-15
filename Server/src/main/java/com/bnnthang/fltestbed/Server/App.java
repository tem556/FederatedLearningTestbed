package com.bnnthang.fltestbed.Server;

import com.bnnthang.fltestbed.Server.AggregationStrategies.NewFedAvg;
import com.bnnthang.fltestbed.models.ServerParameters;
import com.bnnthang.fltestbed.models.TrainingConfiguration;
import com.bnnthang.fltestbed.servers.BaseServer;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws Exception {
        if (args[0].equals("train")) {
//            ML.trainAndEval();
            ML.eval();
        } else {
//            System.out.println("Hello World!");
//            FederatedLearningServer server = new FederatedLearningServerImpl();
//            server.startServer();
            NewFedAvg aggStrategy = new NewFedAvg();
            TrainingConfiguration trainConf = new TrainingConfiguration(Integer.parseInt(args[1]), 50, 8765, aggStrategy);
            ServerOperations servOps = new ServerOperations();
            ServerParameters serverParameters = new ServerParameters(4602, trainConf, servOps);
            BaseServer server = new BaseServer(serverParameters);
            System.out.println("serving...");
            server.serve();
        }
    }
}
