package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.models.ModelUpdate;
import com.bnnthang.fltestbed.commonutils.models.ServerParameters;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Required operations that server has to support.
 */
public interface IServerOperations {
    /**
     * Accept a client to training queue.
     * @param socket socket connection to client
     */
    // TODO: when accepted client sends info if there is model locally,
    //       how to embed the logic for that in common utils?
    void acceptClient(Socket socket) throws IOException;

    /**
     * Deny a client connection.
     * @param socket socket connection to client
     */
    void rejectClient(Socket socket) throws IOException;

    /**
     * Push dataset to clients.
     * @param clients list of accepted clients
     */
    void pushDatasetToClients(List<IClientHandler> clients, float ratio) throws IOException;

    /**
     * Push model to clients.
     * @param clients list of accepted clients
     */
    void pushModelToClients(List<IClientHandler> clients) throws IOException;

    /**
     * Initiate a training round if certain conditions are met.
     * @param serverParameters given server parameters to verify if the conditions for a training round are met
     */
    Boolean trainOrElse(ServerParameters serverParameters) throws IOException;

    /**
     * Aggregate results from clients.
     * @param modelUpdates list of model updates
     * @param aggregationStrategy an aggregation strategy
     */
    void aggregateResults(List<ModelUpdate> modelUpdates,
                          IAggregationStrategy aggregationStrategy) throws Exception;

    /**
     * Check if the server is still amidst of a training round.
     * @return <code>true</code> if any client is still training
     * and <code>false</code> otherwise
     */
    Boolean isTraining();

    void evaluateCurrentModel(List<ModelUpdate> trainingReports) throws IOException;

    void done() throws Exception;
}
