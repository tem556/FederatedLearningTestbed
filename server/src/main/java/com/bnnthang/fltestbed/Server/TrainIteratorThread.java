package com.bnnthang.fltestbed.Server;

import com.bnnthang.fltestbed.Server.AggregationStrategies.AggregationStrategy;
import com.bnnthang.fltestbed.Server.AggregationStrategies.FedAvg;
import org.nd4j.evaluation.IEvaluation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

        // TODO: change hard-coded value
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        Path resultPath = Path.of(String.format("C:/Users/buinn/DoNotTouch/crap/photolabeller/result-%s.txt",
                dtf.format(LocalDateTime.now())));
        try {
            Files.createFile(resultPath);

            for (int currentRound = 0; currentRound < numRounds; ++currentRound) {
                List<ClientTrainThread> trainThreads = clients
                        .stream()
                        .map(ClientTrainThread::new)
                        .collect(Collectors.toList());

                System.out.println("round " + currentRound);

                // TODO: change hard-coded value
                String newModelPath = String.format("C:/Users/buinn/DoNotTouch/crap/photolabeller/newtest-%s.zip",
                        dtf.format(LocalDateTime.now()));
                AggregationStrategy fedAvg = new FedAvg();

                TrainCoordinatorThread trainCoordinatorThread =
                        new TrainCoordinatorThread(trainThreads, fedAvg, baseModelPath, newModelPath);
                trainCoordinatorThread.run();

                // TODO: print to an external file
                String statStr = trainCoordinatorThread.evaluation.stats();
                // TODO: change to a dependency
                Files.write(resultPath, ("\nRound " + currentRound + "---------\n").getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.APPEND);
                Files.write(resultPath, statStr.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                Files.write(resultPath, ("\navg training time = " + trainCoordinatorThread.avgTrainingTimeInSecs + " secs\n").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

                // prepare for next round
                baseModelPath = newModelPath;
                trainThreads.clear();
            }

            // close connections
            for (ClientHandler client : clients) {
                client.done();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
