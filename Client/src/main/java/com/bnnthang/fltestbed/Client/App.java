package com.bnnthang.fltestbed.Client;

import com.beust.jcommander.JCommander;
import com.bnnthang.fltestbed.commonutils.clients.BaseClient;
import com.bnnthang.fltestbed.commonutils.clients.BaseClientOperations;
import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import com.bnnthang.fltestbed.commonutils.clients.IClientOperations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class App {
    private static final Logger _logger = LogManager.getLogger(App.class);

    public static void main(String[] args) throws IOException, InterruptedException {
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
        // TODO: list arguments documentation
        System.out.println("help");
    }

    private static void ml(AppArgs appArgs) throws IOException {
        ML.cifar10TrainAndEval(appArgs.workDir);
    }

    private static void fl(AppArgs appArgs) throws IOException, InterruptedException {
        List<BaseClient> clientPool = new ArrayList<>();

        for (int i = 0; i < appArgs.numClients; ++i) {
            // make client dir if needed
            // TODO: remove this magic value
            String clientDir = appArgs.workDir + "/dirclient" + i;
            Path path = Paths.get(clientDir);
            if (Files.notExists(path))
                Files.createDirectory(path);

            // TODO: remove these magic values
            String pathToModel = clientDir + "/model.zip";
            String pathToDataset = clientDir + "/dataset";

            IClientLocalRepository localRepository = new Cifar10Repository(pathToModel, pathToDataset);
            IClientOperations clientOperations = new BaseClientOperations(localRepository);
            BaseClient client = new BaseClient(appArgs.host, appArgs.port, 5000, clientOperations);
            client.start();

            _logger.debug("running " + i);

            clientPool.add(client);
        }

        // wait for clients to serve
        for (BaseClient client : clientPool) {
            client.join();
        }
    }
}
