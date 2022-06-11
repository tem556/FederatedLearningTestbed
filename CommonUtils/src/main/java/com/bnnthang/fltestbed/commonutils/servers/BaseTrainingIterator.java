package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.models.TrainingConfiguration;
import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import lombok.AllArgsConstructor;
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

    @Override
    public void run() {
        try {
            // offload dataset to client
            operations.pushDatasetToClients(clients, configuration.getDatasetRatio());

            // repeat the process a certain number of times
            for (int currentRound = 1;
                 currentRound <= configuration.getRounds();
                 ++currentRound) {

                _logger.info("current round = " + currentRound);

                // offload model to clients
                // TODO: add logic for sending weights only
                operations.pushModelToClients(clients);
                _logger.info("pushed updated model to all clients");

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

                operations.aggregateResults(reports.stream().map(TrainingReport::getModelUpdate).collect(Collectors.toList()), configuration.getAggregationStrategy());
                _logger.info("aggregated updates");

                // evaluate
                operations.evaluateCurrentModel(reports);
                _logger.info("evaluated new model");

                // deallocate arrays
                for (TrainingReport report : reports) {
                    report.getModelUpdate().getWeight().close();
                }
            }

            operations.done();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
