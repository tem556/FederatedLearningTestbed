package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.models.ModelUpdate;
import com.bnnthang.fltestbed.commonutils.models.TrainingConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simple implementation for a training iteration.
 */
@AllArgsConstructor
public final class BaseTrainingIterator extends Thread {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTrainingIterator.class);

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
        boolean useDropping = configuration.getUseDropping();
        List<Integer> dropping = configuration.getDropping();

        // to randomize the dropping of clients
        Random ran = new Random();
        try {
            // offload dataset to client
            operations.pushDatasetToClients(clients, configuration.getDatasetRatio());

            // repeat the process a certain number of times
            for (int currentRound = 1; currentRound <= configuration.getRounds(); ++currentRound) {

                if (useDropping && (dropping.get(currentRound) != 0)) {
                    dropClients(dropping.get(currentRound), ran);
                    LOGGER.info("Removed " + dropping.get(currentRound) + " clients at round " + currentRound);
                }

                Long t0 = System.currentTimeMillis();

                LOGGER.info("current round = " + currentRound);

                // offload model to clients
                operations.pushModelToClients(clients);
                LOGGER.info("pushed updated model to all clients");
                Long t1 = System.currentTimeMillis();

                for (IClientHandler client : clients) {
                    client.startTraining();
                }
                LOGGER.info("asked all clients to train");

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
                List<ModelUpdate> updates = new ArrayList<>();
                LOGGER.info("getting training reports...");
                for (IClientHandler client : clients) {
                    ModelUpdate report = client.getTrainingReport();
                    if (report == null) {
                        throw new UnexpectedException("received null training report");
                    }
                    updates.add(report);
                }
                LOGGER.info("got all training reports");

                Long t3 = System.currentTimeMillis();

                operations.aggregateResults(updates, configuration.getAggregationStrategy());
                LOGGER.info("aggregated updates");

                Thread evalThread = new Thread(new EvaluationRunnable(operations, updates));
                evalThread.start();

                // keep the connections alive
                while (evalThread.isAlive()) {
                    for (IClientHandler clientHandler : clients) {
                        clientHandler.isTraining();
                    }

                    // delay a bit
                    sleep(configuration.getPollInterval());
                }
                LOGGER.info("evaluated new model");

                // deallocate arrays
                for (ModelUpdate update : updates) {
                    update.dispose();
                }

                Long t4 = System.currentTimeMillis();

                serverTimeTracker.addRound(t0, t1, t2, t3, t4);
            }

            operations.done();
        } catch (Exception e) {
            LOGGER.error("EXCEPTION", e);
        }
    }

    /*
     * Removes the amount of clients specified in the first element in @param
     * dropping, the choice of clients is random.
     */
    public void dropClients(Integer nClients, Random ran) throws Exception {
        // if to be removed clients more than available in the pool
        if (nClients > clients.size()) {
            LOGGER.info("Cancelled dropping clients, remaining number of clients not enough");
            return;
        }
        for (int i = 0; i < nClients; i++) {
            // chooses a random client from the pool to drop
            int randomClient = ran.nextInt(clients.size());
            IClientHandler currentClient = clients.get(randomClient);
            currentClient.done();
            clients.remove(randomClient);
        }
    }
}
