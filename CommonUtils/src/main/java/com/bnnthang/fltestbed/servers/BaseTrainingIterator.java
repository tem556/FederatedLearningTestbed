package com.bnnthang.fltestbed.servers;

import com.bnnthang.fltestbed.models.TrainingConfiguration;
import com.bnnthang.fltestbed.models.TrainingReport;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple implementation for a training iteration.
 */
@AllArgsConstructor
public final class BaseTrainingIterator extends Thread {
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
            operations.pushDatasetToClients(clients);

            // repeat the process a certain number of times
            for (Integer currentRound = 1;
                 currentRound <= configuration.getRounds();
                 ++currentRound) {

                // offload model to clients
                // TODO: add logic for sending weights only
                operations.pushModelToClients(clients);

                for (IClientHandler client : clients) {
                    client.startTraining();
                }

                // wait for trainings to finish
                do {
                    // delay a bit
                    sleep(configuration.getPollInterval());

                    // check if clients finish
                    Boolean areClientsTraining = clients.stream()
                            .map(IClientHandler::isTraining)
                            .reduce((x, y) -> x && y)
                            .orElse(false);

                    // break if clients finish
                    if (!areClientsTraining) {
                        break;
                    }
                } while (true);

                // aggregate results
                List<TrainingReport> reports = clients.stream()
                        .map(IClientHandler::getTrainingReport)
                        .collect(Collectors.toList());
                operations.aggregateResults(reports,
                        configuration.getAggregationStrategy());

                // TODO: add evaluation
            }

            // terminate connections
            for (IClientHandler client : clients) {
                client.done();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
