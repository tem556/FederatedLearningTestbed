package com.bnnthang.fltestbed.Server;

import com.beust.jcommander.JCommander;
import com.bnnthang.fltestbed.Server.AggregationStrategies.FedAvg;
import com.bnnthang.fltestbed.Server.Repositories.ServerLocalRepositoryFactory;
import com.bnnthang.fltestbed.commonutils.models.ServerParameters;
import com.bnnthang.fltestbed.commonutils.models.TrainingConfiguration;
import com.bnnthang.fltestbed.commonutils.servers.BaseServer;
import com.bnnthang.fltestbed.commonutils.servers.BaseServerOperations;
import com.bnnthang.fltestbed.commonutils.servers.IServerLocalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.net.URISyntaxException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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

    private static List<Integer> getDropping(ArrayList<JSONObject> jsonObjects, Integer rounds) {
        Integer[] droppingList = new Integer[rounds];
        Arrays.fill(droppingList, 0);
        Iterator<JSONObject> iter = jsonObjects.iterator();
        while (iter.hasNext()) {
            JSONObject current = iter.next();
            int round = ((Long) current.get("round")).intValue();
            int nClients = ((Long) current.get("#clients")).intValue();
            droppingList[round] = nClients;
        }
        return Arrays.asList(droppingList);
    }

    private static ArrayList<Double> getWeights(AppArgs args, boolean evenLabelD, ArrayList<Double> dByClient,
            ArrayList<ArrayList<Double>> dByLabels) throws IOException {
        ArrayList<Double> weights;
        if (!args.useConfig) {
            Double[] weightsArr = new Double[args.numClients];
            Arrays.fill(weightsArr, 1.0 / args.numClients);
            weights = new ArrayList<Double>(Arrays.asList(weightsArr));
        } else if (args.useConfig && evenLabelD) {
            weights = dByClient;
        } else {
            ArrayList<Double> res = new ArrayList<Double>();
            for (int i = 0; i < args.numClients; i++) {
                // get the sum of the list for the ith client
                Double curr = (dByLabels.get(i)).stream().mapToDouble(a -> (double) a).sum();
                res.add(curr / args.numClients);
            }
            weights = res;
        }
        // Make sure the weights are appropriate
        if (weights.size() != args.numClients) {
            throw new IOException("Invalid number of weights in JSON");
        }
        return weights;
    }

    private static void fl(AppArgs args) throws IOException, InterruptedException, URISyntaxException {
        // Make sure jsonObject is only dereferenced when useConfig is true
        JSONObject jsonObject = null;
        boolean evenLabelDistribution = false;
        ArrayList<Double> distributionRatiosByClient = null;
        ArrayList<ArrayList<Double>> distributionRatiosByLabels = null;
        boolean useDropping = false;
        List<Integer> dropping = null;
        if (args.useConfig) {
            try {
                JSONParser parser = new JSONParser();
                jsonObject = (JSONObject) parser.parse(new FileReader(args.workDir + "/config.json"));
                evenLabelDistribution = (boolean) jsonObject.get("evenLabelDistributionByClient");
                distributionRatiosByClient = (ArrayList<Double>) jsonObject.get("distributionRatiosByClient");
                distributionRatiosByLabels = (ArrayList<ArrayList<Double>>) jsonObject.get("distributionRatiosByLabels");
                dropping = getDropping((ArrayList<JSONObject>) jsonObject.get("dropping"), args.rounds);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ArrayList<Double> weights = getWeights(args, evenLabelDistribution, distributionRatiosByClient,
                distributionRatiosByLabels);
        TrainingConfiguration trainingConfiguration = new TrainingConfiguration(args.numClients, args.rounds, 1000,
                new FedAvg(weights), args.datasetRatio.floatValue(), args.useConfig, evenLabelDistribution,
                distributionRatiosByClient, distributionRatiosByLabels, useDropping, dropping);

        IServerLocalRepository localRepository = ServerLocalRepositoryFactory.getRepository(
                args.useHealthDataset,
                args.workDir,
                args.useConfig,
                trainingConfiguration);
        BaseServerOperations serverOperations = new BaseServerOperations(localRepository);

        ServerParameters serverParameters = new ServerParameters(args.port, trainingConfiguration, serverOperations);
        BaseServer server = new BaseServer(serverParameters);
        server.start();
        _logger.info("Server is up.");
        server.join();
    }
}
