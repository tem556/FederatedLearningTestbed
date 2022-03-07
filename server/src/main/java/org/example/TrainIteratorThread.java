package org.example;

import org.nd4j.evaluation.IEvaluation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrainIteratorThread extends Thread {
    private List<ClientHandler> clients;
    private int numRounds;
    private List<IEvaluation> evaluations;
    private String baseModelPath;

    public TrainIteratorThread(List<ClientHandler> clients, String baseModelPath, int numRounds) {
        super();
        this.clients = clients;
        this.baseModelPath = baseModelPath;
        this.numRounds = numRounds;
        this.evaluations = new ArrayList<>();
    }

    @Override
    public void run() {
        System.out.println("init training...");

        for (int currentRound = 0; currentRound < numRounds; ++currentRound) {
            List<ClientTrainThread> trainThreads = clients
                    .stream()
                    .map(ClientTrainThread::new)
                    .collect(Collectors.toList());

            System.out.println("round " + currentRound);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
            String newModelPath = String.format("C:/Users/buinn/DoNotTouch/crap/photolabeller/newtest-%s.zip",
                    dtf.format(LocalDateTime.now()));
            AggregationStrategy fedAvg = new FedAvg();

            TrainCoordinatorThread trainCoordinatorThread =
                    new TrainCoordinatorThread(trainThreads, fedAvg, baseModelPath, newModelPath);
            trainCoordinatorThread.run();

            // TODO: print to an external file
            System.out.println(trainCoordinatorThread.evaluation.stats());

            // prepare for next round
            baseModelPath = newModelPath;
            trainThreads.clear();
        }

        // close connections
        for (ClientHandler client : clients) {
            try {
                client.done();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
