package org.example;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.*;


public class FederatedLearningServerImpl implements FederatedLearningServer {
    // TODO: change hard-coded values
    private static final int PORT = 4602;
    private static final int MIN_CLIENTS = 1;
    private static final int TRAINING_ROUNDS = 2;

    private final ServerSocket serverSocket;
    private final ArrayList<ClientHandler> clientPool;
    private TrainIteratorThread trainIteratorThread = null;

    FederatedLearningServerImpl() throws IOException {
        clientPool = new ArrayList<>();
        serverSocket = new ServerSocket(PORT);
        trainIteratorThread = null;
    }

    @Override
    public void startServer() throws IOException, InterruptedException {
        System.out.println("server starting...");

        do {
            Socket client = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandlerImpl(client);

            boolean running = trainIteratorThread != null && trainIteratorThread.isAlive();
            if (!running) {
                // clean garbage from previous training
                if (trainIteratorThread != null) {
                    trainIteratorThread = null;
                    clientPool.clear();
                }
            }

            if (!running && clientPool.size() < MIN_CLIENTS) {
                clientHandler.register();
                clientPool.add(clientHandler);

                if (clientPool.size() >= MIN_CLIENTS) {
                    System.out.println("init training...");

                    // TODO: change hard-coded path
                    trainIteratorThread = new TrainIteratorThread(
                            clientPool,
                            "C:/Users/buinn/DoNotTouch/crap/photolabeller/newmodel.zip",
                            TRAINING_ROUNDS);
                    trainIteratorThread.start();
                }
            } else {
                clientHandler.reject();
            }
        } while (true);
    }
}
