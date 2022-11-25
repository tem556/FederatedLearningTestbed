package com.bnnthang.fltestbed.Server;

import com.beust.jcommander.JCommander;
import com.bnnthang.fltestbed.Server.AggregationStrategies.FedAvg;
import com.bnnthang.fltestbed.Server.Repositories.ChestXrayRepository;
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
        } else if (appArgs.model) {
            // TODO: replace this with factory pattern (perhaps in commonutils)
            if (appArgs.useHealthDataset) {
                ML.getBaseChestXrayModel(appArgs.workDir);
            } else {
                ML.getBaseCifar10Model(appArgs.workDir);
            }
        } else {
            _logger.info("no training model specified.");
        }
    }

    private static void help() {
        // TODO: print arguments documentation
        System.out.println("help");
    }

    private static void ml(AppArgs args) throws IOException {
        if (args.useHealthDataset) {
            ML.trainAndEvalPneumonia(args.rounds, args.datasetRatio, args.workDir);
        } else {
            ML.trainAndEvalCifar10(args.rounds, args.datasetRatio, args.workDir);
        }
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

        TrainingConfiguration trainingConfiguration = new TrainingConfiguration(args.numClients, args.rounds, 1000,
                new FedAvg(), args.datasetRatio.floatValue(), args.useConfig);
        trainingConfiguration.setJsonObject(jsonObject);

        // TODO: replace this with Factory pattern.
        IServerOperations serverOperations;
        if (args.useHealthDataset) {
            serverOperations = new BaseServerOperations(new ChestXrayRepository(args.workDir, args.useConfig, jsonObject));
        } else {
            serverOperations = new BaseServerOperations(new Cifar10Repository(args.workDir, args.useConfig, jsonObject, false));
        }
        
        ServerParameters serverParameters = new ServerParameters(args.port, trainingConfiguration, serverOperations);
        BaseServer server = new BaseServer(serverParameters);
        server.start();
        _logger.info("Server is up.");
        server.join();
    }
}
