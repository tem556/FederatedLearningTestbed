package com.bnnthang.fltestbed.Server;

import com.beust.jcommander.JCommander;
import com.bnnthang.fltestbed.Server.AggregationStrategies.FedAvg;
import com.bnnthang.fltestbed.Server.Repositories.Cifar10Repository;
import com.bnnthang.fltestbed.commonutils.models.ServerParameters;
import com.bnnthang.fltestbed.commonutils.models.TrainingConfiguration;
import com.bnnthang.fltestbed.commonutils.servers.BaseServer;
import com.bnnthang.fltestbed.commonutils.servers.BaseServerOperations;
import com.bnnthang.fltestbed.commonutils.servers.IServerOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import org.json.simple.*;
import org.json.simple.parser.*;

public class App {
    private static final Logger _logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        AppArgs appArgs = new AppArgs();
        JCommander.newBuilder().addObject(appArgs).build().parse(args);

        if (appArgs.fl) {
            fl(appArgs);
        } else if (appArgs.ml) {
            ml(appArgs);
        } else {
            _logger.info("no training model specified.");
        }
    }

    private static void help() {
        // TODO: print arguments documentation
        System.out.println("help");
    }

    private static void ml(AppArgs args) throws IOException {
        ML.trainAndEval(args.rounds, args.datasetRatio, args.workDir);
    }

    private static void fl(AppArgs args) throws IOException, InterruptedException, URISyntaxException {
        // Make sure jsonObject is only dereferenced when useConfig is true
        JSONObject jsonObject = null;
        if (args.useConfig) {
            try {
                JSONParser parser = new JSONParser();
                jsonObject = (JSONObject) parser.parse(new FileReader(args.workDir + "/config.json"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TrainingConfiguration trainingConfiguration = new TrainingConfiguration(args.numClients, args.rounds, 5000, new FedAvg(), args.datasetRatio, args.useConfig);
        trainingConfiguration.setJsonObject(jsonObject);
        IServerOperations serverOperations = new BaseServerOperations(new Cifar10Repository(args.workDir, args.useConfig, jsonObject));
        ServerParameters serverParameters = new ServerParameters(args.port, trainingConfiguration, serverOperations);
        BaseServer server = new BaseServer(serverParameters);
        server.start();
        server.join();
    }
}
