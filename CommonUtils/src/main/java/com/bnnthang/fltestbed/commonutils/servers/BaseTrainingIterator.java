package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.models.TrainingConfiguration;
import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple implementation for a training iteration.
 */
@AllArgsConstructor
public final class BaseTrainingIterator extends Thread {
    /**
     * Logger.
     */
    private static final Logger _logger = LoggerFactory.getLogger(BaseTrainingIterator.class);

    /**
     * Supported operations.
     */
    @NonNull
    private IServerOperations operations;

    /**
     * Involved clients in training iteration.
     */
    @NonNull
    private List<IClientHandler> clients;

    /**
     * Training configuration.
     */
    @NonNull
    private TrainingConfiguration configuration;

    @NonNull
    @Getter
    private ServerTimeTracker serverTimeTracker;

    @Override
    public void run() {
        try {
            // offload dataset to client
            operations.pushDatasetToClients(clients, configuration.getDatasetRatio());

	        _logger.info("Creating Aggregated Log file");

////             make log file for aggregated time
//            File AgLogFile = new File(logFolder, "aggregated-log.csv");
//            AgLogFile.createNewFile();
//            CSVWriter _logWriter = new CSVWriter(new FileWriter(AgLogFile));
//            _logWriter.writeNext(new String[] {"Round no.", "Aggregated time(ms)"});

            // repeat the process a certain number of times
            for (int currentRound = 1;
                 currentRound <= configuration.getRounds();
                 ++currentRound) {

//                LocalDateTime startTime = LocalDateTime.now();

                Long t0 = System.currentTimeMillis();

                _logger.info("current round = " + currentRound);

                // offload model to clients
                operations.pushModelToClients(clients);
                _logger.info("pushed updated model to all clients");
                Long t1 = System.currentTimeMillis();

                for (IClientHandler client : clients) {
                    client.startTraining();
                }
                _logger.info("asked all clients to train");

                // wait for trainings to finish
                do {
                    // delay a bit
                    sleep(configuration.getPollInterval());

                    // check if clients finish
                    Boolean areClientsTraining = clients.stream()
                            .map(IClientHandler::isTraining)
                            .reduce(false, Boolean::logicalOr);

                    // break if clients finish
                    if (!areClientsTraining) {
                        break;
                    }
                } while (true);

                Long t2 = System.currentTimeMillis();

                // aggregate results
                // TODO: parallelize this
                List<TrainingReport> reports = new ArrayList<>();
                _logger.info("getting training reports...");
                for (IClientHandler client : clients) {
                    TrainingReport report = client.getTrainingReport();
                    if (report == null) {
                        throw new UnexpectedException("received null training report");
                    }
                    reports.add(report);
                }
                _logger.info("got all training reports");

                Long t3 = System.currentTimeMillis();

                operations.aggregateResults(reports.stream().map(TrainingReport::getModelUpdate).collect(Collectors.toList()), configuration.getAggregationStrategy());
                _logger.info("aggregated updates");

                // evaluate
                operations.evaluateCurrentModel(reports);
                _logger.info("evaluated new model");

//                LocalDateTime endTime = LocalDateTime.now();
//                Double roundTime = TimeUtils.millisecondsBetween(startTime, endTime);

                // deallocate arrays
                for (TrainingReport report : reports) {
                    report.getModelUpdate().getWeight().close();
                }

                Long t4 = System.currentTimeMillis();

                serverTimeTracker.addRound(t0, t1, t2, t3, t4);
//                _logWriter.writeNext(new String[] {String.valueOf(currentRound), String.valueOf(roundTime)});
            }

//            _logWriter.close();

            operations.done();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
