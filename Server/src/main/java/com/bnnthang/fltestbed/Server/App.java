package com.bnnthang.fltestbed.Server;

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
    private static final int DEFAULT_PORT = 4602;
    private static final int DEFAULT_MIN_CLIENTS = 1;
    private static final int DEFAULT_ROUNDS = 3;
    private static final String DEFAULT_MODEL_DIR = "C:/Users/buinn/DoNotTouch/crap/photolabeller";
    private static final String DEFAULT_APK_PATH = "C:\\Users\\buinn\\Repos\\FederatedLearningTestbed\\AndroidClient\\app\\build\\intermediates\\apk\\debug\\app-debug.apk";;
    private static final float DEFAULT_DATASET_RATIO = 1.0F;
    private static final boolean DEFAULT_USE_CONFIG = false;
    private static final JSONObject DEFAULT_JSON_OBJECT = null;

    public static void main(String[] args) throws Exception {
        _logger.debug("hello world");

        switch (args[0]) {
            case "--help":
                help();
                break;
            case "--ml":
                ml(args);
                break;
            case "--fl":
                fl(args);
                break;
            default:
                throw new UnsupportedOperationException("unsupported argument: " + args[0]);
        }
    }

    private static void help() {
        // TODO: print arguments documentation
        System.out.println("help");
    }

    private static void ml(String[] args) throws IOException {
        int rounds = DEFAULT_ROUNDS;
        String workDir = DEFAULT_MODEL_DIR;
        float ratio = DEFAULT_DATASET_RATIO;

        for (int i = 1; i < args.length; i += 2) {
            switch (args[i]) {
                case "--rounds":
                    rounds = Integer.parseInt(args[i + 1]);
                    break;
                case "--workdir":
                    workDir = args[i + 1];
                    break;
                case "--datasetratio":
                    ratio = Float.parseFloat(args[i + 1]);
                    break;
            }
        }
        ML.trainAndEval(rounds, ratio, workDir);
//        ML.trainAndEvalDefault();
    }

    private static void fl(String[] args) throws IOException, InterruptedException, URISyntaxException {
        int port = DEFAULT_PORT;
        int minClients = DEFAULT_MIN_CLIENTS;
        int rounds = DEFAULT_ROUNDS;
        String workDir = DEFAULT_MODEL_DIR;
        String apkPath = DEFAULT_APK_PATH;
        float ratio = DEFAULT_DATASET_RATIO;
        boolean useConfig = DEFAULT_USE_CONFIG;
        // Make sure jsonObject is only dereferenced when useConfig is true
        JSONObject jsonObject = DEFAULT_JSON_OBJECT;
        for (int i = 1; i < args.length; i += 2) {
            switch (args[i]) {
                case "--port":
                    port = Integer.parseInt(args[i + 1]);
                    break;
                case "--minClients":
                    minClients = Integer.parseInt(args[i + 1]);
                    break;
                case "--rounds":
                    rounds = Integer.parseInt(args[i + 1]);
                    break;
                case "--workdir":
                    workDir = args[i + 1];
                    break;
                case "--datasetratio":
                    ratio = Float.parseFloat(args[i + 1]);
                    break;
                case "--apk":
                    apkPath = args[i + 1];
                    break;
                case "--config":
                    useConfig = Boolean.parseBoolean(args[i + 1]);
                    if (useConfig){
                        try {
                            JSONParser parser = new JSONParser();
                            jsonObject = (JSONObject)parser.parse(new FileReader(workDir + "/config.json"));
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("unsupported argument: " + args[i]);
            }
        }

        TrainingConfiguration trainingConfiguration = new TrainingConfiguration(minClients, rounds, 5000,
                new FedAvg(), ratio, useConfig);
        trainingConfiguration.setJsonObject(jsonObject);
        IServerOperations serverOperations = new BaseServerOperations(new Cifar10Repository(workDir, useConfig, jsonObject));
        ServerParameters serverParameters = new ServerParameters(port, trainingConfiguration, serverOperations);
        BaseServer server = new BaseServer(serverParameters);
        server.start();
        server.join();
    }
}
