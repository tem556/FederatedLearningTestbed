package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.models.TrainingConfiguration;
import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.json.simple.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.lang.*;

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
        JSONObject jsonObject = configuration.getJsonObject();
        boolean useDropping = false;
        // dummy list, to avoid raising error
        ArrayList<JSONObject> dropping = new ArrayList<>();
        if (configuration.getUseConfig()){
            useDropping = (boolean) jsonObject.get("useDropping");
            dropping = (ArrayList<JSONObject>) jsonObject.get("dropping");
        }
        // to randomize the dropping of clients
        Random ran = new Random();
        try {
            // offload dataset to client
            operations.pushDatasetToClients(clients, configuration.getDatasetRatio());

            // repeat the process a certain number of times
            for (int currentRound = 1;
                 currentRound <= configuration.getRounds();
                 ++currentRound) {

                if (useDropping && !dropping.isEmpty()){
                    dropping = dropClients(currentRound, dropping, ran);
                }

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

                // deallocate arrays
                for (TrainingReport report : reports) {
                    report.getModelUpdate().getWeight().close();
                }

                Long t4 = System.currentTimeMillis();

                serverTimeTracker.addRound(t0, t1, t2, t3, t4);
            }


            operations.done();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * Removes the amount of clients specified in the first element in @param dropping, the choice of clients is random.
    */
    public ArrayList<JSONObject> dropClients(int currentRound, ArrayList<JSONObject> dropping, Random ran) throws Exception{
        JSONObject current = dropping.get(0);
        int round = ((Long)current.get("round")).intValue();
        int nClients = ((Long)current.get("#clients")).intValue();
        if (round != currentRound) return dropping;
        // if to be removed clients more than available in the pool
        if (nClients > clients.size()){
            _logger.info("Cancelled dropping clients, remaining number of clients not enough");
            return dropping;
        }
        for (int i = 0; i < nClients; i++){
            // chooses a random client from the pool to drop
            int randomClient = ran.nextInt(clients.size());
            IClientHandler currentClient = clients.get(randomClient);
            currentClient.done();
            clients.remove(randomClient);
        }
        _logger.info("Removed " + nClients + " clients at round " +round);
        dropping.remove(0);
        return dropping;
    }
}
