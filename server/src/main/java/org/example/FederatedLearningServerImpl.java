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
    private boolean running;
    private ArrayList<ClientHandler> clientPool;

    FederatedLearningServerImpl() throws IOException {
        clientPool = new ArrayList<ClientHandler>();
        serverSocket = new ServerSocket(PORT);
    }

    @Override
    public void startServer() throws IOException, InterruptedException {
        System.out.println("server starting...");

        while (true) {
            Socket client = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandlerImpl(client);
            if (!running && clientPool.size() < MIN_CLIENTS) {
                clientHandler.register();
                clientPool.add(clientHandler);

                if (clientPool.size() >= MIN_CLIENTS) {
                    // TODO: start train process

                    List<ClientTrainThread> trainThreads = clientPool
                            .stream()
                            .map(x -> new ClientTrainThread(x, 1))
                            .collect(Collectors.toList());

                    Thread trainManagerThread = new Thread() {
                        @Override
                        public void run() {
                            super.run();

                            running = true;

                            try {
                                // start training
                                for (int i = 0; i < trainThreads.size(); ++i) {
                                    trainThreads.get(i).start();
                                }

                                // wait for result
                                for (int i = 0; i < trainThreads.size(); ++i) {
                                    trainThreads.get(i).join();
                                }

                                List<INDArray> updates = trainThreads.stream().map(x -> x.weightUpdates).collect(Collectors.toList());
                                aggregate(updates);
                                running = false;

                                for (int i = 0; i < clientPool.size(); ++i) {
                                    clientPool.get(i).close();
                                }
                                clientPool.clear();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    trainManagerThread.start();
                }

            } else {
                clientHandler.reject();
            }
        }
    }

    @Override
    public void aggregate(List<INDArray> updates) throws IOException {
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork("C:/Users/buinn/DoNotTouch/crap/photolabeller/model.zip");
        long[] dimensions = model.getLayer(3).params().shape();
        INDArray base = Nd4j.zeros(dimensions[0], dimensions[1]);
        INDArray sumUpdates = updates.stream().reduce(base, (x, y) -> x.add(y));
        INDArray avgUpdates = sumUpdates.div(updates.size());
        model.getLayer(3).setParams(avgUpdates);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String path = String.format("C:/Users/buinn/DoNotTouch/crap/photolabeller/newtest-%s.zip", dtf.format(LocalDateTime.now()));
        ModelSerializer.writeModel(model, path, true);
    }
}
