package org.example;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.joda.time.DateTime;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.net.ServerSocket;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;


public class FederatedLearningServerImpl implements FederatedLearningServer {
    private static final int PORT = 4602;
    private static final int MIN_CLIENTS = 1;
    private static final int TRAINING_ROUNDS = 2;

    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clientPool;
    private TrainCoordinatorThread trainCoordinatorThread = null;

    FederatedLearningServerImpl() throws IOException {
        clientPool = new ArrayList<>();
        serverSocket = new ServerSocket(PORT);
        trainCoordinatorThread = null;
    }

    @Override
    public void startServer() throws IOException, InterruptedException {
        System.out.println("server starting...");

        while (true) {
            Socket client = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandlerImpl(client);
            boolean running = trainCoordinatorThread != null && trainCoordinatorThread.isAlive();

            if (!running && clientPool.size() < MIN_CLIENTS) {
                clientHandler.register();
                clientPool.add(clientHandler);

                if (clientPool.size() >= MIN_CLIENTS) {
                    System.out.println("init training...");

                    List<ClientTrainThread> trainThreads = clientPool
                            .stream()
                            .map(x -> new ClientTrainThread(x, 1))
                            .collect(Collectors.toList());

                    String baseModelPath = "C:/Users/buinn/DoNotTouch/crap/photolabeller/newmodel.zip";
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
                    String newModelPath = String.format("C:/Users/buinn/DoNotTouch/crap/photolabeller/newtest-%s.zip", dtf.format(LocalDateTime.now()));
                    AggregationStrategy fedAvg = new FedAvg();
                    trainCoordinatorThread = new TrainCoordinatorThread(trainThreads, fedAvg, baseModelPath, newModelPath);
                    trainCoordinatorThread.start();
                }
            } else {
                clientHandler.reject();
            }
        }
    }
}
