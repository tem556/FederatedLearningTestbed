package org.example;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.*;


public class FederatedLearningServerImpl implements FederatedLearningServer {
    private static final int PORT = 4602;
    private static final int MIN_CLIENTS = 1;
    private static final int TRAINING_ROUNDS = 2;

    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clientPool;
//    private TrainCoordinatorThread trainCoordinatorThread = null;
    private TrainIteratorThread trainIteratorThread = null;

    FederatedLearningServerImpl() throws IOException {
        clientPool = new ArrayList<>();
        serverSocket = new ServerSocket(PORT);
//        trainCoordinatorThread = null;
        trainIteratorThread = null;
    }

    @Override
    public void startServer() throws IOException, InterruptedException {
        System.out.println("server starting...");

        while (true) {
            Socket client = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandlerImpl(client);

            boolean running = trainIteratorThread != null && trainIteratorThread.isAlive();
            if (!running) trainIteratorThread = null;

            if (!running && clientPool.size() < MIN_CLIENTS) {
                clientHandler.register();
                clientPool.add(clientHandler);

                if (clientPool.size() >= MIN_CLIENTS) {
                    System.out.println("init training...");

//                    List<ClientTrainThread> trainThreads = clientPool
//                            .stream()
//                            .map(x -> new ClientTrainThread(x, 1))
//                            .collect(Collectors.toList());
//
//                    String baseModelPath = "C:/Users/buinn/DoNotTouch/crap/photolabeller/newmodel.zip";
//                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
//                    String newModelPath = String.format("C:/Users/buinn/DoNotTouch/crap/photolabeller/newtest-%s.zip", dtf.format(LocalDateTime.now()));
//                    AggregationStrategy fedAvg = new FedAvg();
//                    trainCoordinatorThread = new TrainCoordinatorThread(trainThreads, fedAvg, baseModelPath, newModelPath);
//                    trainCoordinatorThread.start();
                    trainIteratorThread = new TrainIteratorThread(
                            clientPool,
                            "C:/Users/buinn/DoNotTouch/crap/photolabeller/newmodel.zip",
                            TRAINING_ROUNDS);
                    trainIteratorThread.start();
                }
            } else {
                clientHandler.reject();
            }
        }
    }
}
