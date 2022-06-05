package com.bnnthang.fltestbed.Server;

import com.bnnthang.fltestbed.Server.AggregationStrategies.FedAvg;
import com.bnnthang.fltestbed.Server.Repositories.Cifar10Repository;
import com.bnnthang.fltestbed.commonutils.models.ServerParameters;
import com.bnnthang.fltestbed.commonutils.models.TrainingConfiguration;
import com.bnnthang.fltestbed.commonutils.servers.BaseServer;
import com.bnnthang.fltestbed.commonutils.servers.BaseServerOperations;
import com.bnnthang.fltestbed.commonutils.servers.IServerOperations;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Core;

import java.io.IOException;
import java.net.URISyntaxException;

public class App {
    private static final Logger _logger = LogManager.getLogger(App.class);
    private static final int DEFAULT_PORT = 4602;
    private static final int DEFAULT_MIN_CLIENTS = 1;
    private static final int DEFAULT_ROUNDS = 3;
    private static final String DEFAULT_MODEL_DIR = "C:/Users/buinn/DoNotTouch/crap/photolabeller";
    private static final String DEFAULT_APK_PATH = "C:\\Users\\buinn\\Repos\\FederatedLearningTestbed\\AndroidClient\\app\\build\\intermediates\\apk\\debug\\app-debug.apk";;
    private static final float DEFAULT_DATASET_RATIO = 1.0F;

    public static void main(String[] args) throws Exception {
        _logger.debug("hello world");

        // load native library if needed
        if (args[0].equals("--native")) {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            args = ArrayUtils.remove(args, 0);
        }

        switch (args[0]) {
            case "--help":
                help();
                break;
            case "--ml":
                ml();
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

    private static void ml() throws IOException {
//        ML.trainAndEval();
        ML.trainAndEvalDefault();
    }

    private static void fl(String[] args) throws IOException, InterruptedException, URISyntaxException {
        int port = DEFAULT_PORT;
        int minClients = DEFAULT_MIN_CLIENTS;
        int rounds = DEFAULT_ROUNDS;
        String workDir = DEFAULT_MODEL_DIR;
        String apkPath = DEFAULT_APK_PATH;
        float ratio = DEFAULT_DATASET_RATIO;
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
                default:
                    throw new UnsupportedOperationException("unsupported argument: " + args[i]);
            }
        }

        TrainingConfiguration trainingConfiguration = new TrainingConfiguration(minClients, rounds, 5000, new FedAvg(), ratio);
        IServerOperations serverOperations = new BaseServerOperations(new Cifar10Repository(workDir));
        ServerParameters serverParameters = new ServerParameters(port, trainingConfiguration, serverOperations);
        BaseServer server = new BaseServer(serverParameters);
        server.start();
        server.join();
    }
}
