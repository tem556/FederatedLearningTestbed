package com.bnnthang.fltestbed.Server;

import com.bnnthang.fltestbed.Server.AggregationStrategies.NewFedAvg;
import com.bnnthang.fltestbed.Server.Repositories.Cifar10Repository;
import com.bnnthang.fltestbed.commonutils.models.ServerParameters;
import com.bnnthang.fltestbed.commonutils.models.TrainingConfiguration;
import com.bnnthang.fltestbed.commonutils.servers.BaseServer;
import com.bnnthang.fltestbed.commonutils.servers.BaseServerOperations;
import com.bnnthang.fltestbed.commonutils.servers.IServerOperations;
import org.opencv.core.Core;

import java.io.IOException;

public class App {
    private static final int DEFAULT_PORT = 4602;
    private static final int DEFAULT_MIN_CLIENTS = 1;
    private static final int DEFAULT_ROUNDS = 3;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws Exception {
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
        ML.trainAndEval();
    }

    private static void fl(String[] args) throws IOException, InterruptedException {
        int port = DEFAULT_PORT;
        int minClients = DEFAULT_MIN_CLIENTS;
        int rounds = DEFAULT_ROUNDS;
        for (int i = 1; i < args.length; ++i) {
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
                default:
                    throw new UnsupportedOperationException("unsupported argument: " + args[i]);
            }
        }

        TrainingConfiguration trainingConfiguration = new TrainingConfiguration(minClients, rounds, 5000, new NewFedAvg());
        IServerOperations serverOperations = new BaseServerOperations(new Cifar10Repository());
        ServerParameters serverParameters = new ServerParameters(port, trainingConfiguration, serverOperations);
        BaseServer server = new BaseServer(serverParameters);
        server.start();
        server.join();
    }
}
