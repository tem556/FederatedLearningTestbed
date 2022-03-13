package com.bnnthang.fltestbed.servers;

import com.bnnthang.fltestbed.models.ServerParameters;
import com.bnnthang.fltestbed.models.TrainingReport;

import java.net.Socket;
import java.util.List;

/**
 * Required operations that server has to support.
 */
public interface IServerOperations {
    /**
     * Accept a client to training queue.
     * @param socket socket connection to client
     * @param acceptedClients list of accepted clients waiting for
     *                        training instruction
     * @throws Exception
     */
    // TODO: when accepted client sends info if there is model locally,
    //  how to embed the logic for that in common utils?
    void acceptClient(Socket socket,
                      List<IClientHandler> acceptedClients)
            throws Exception;

    /**
     * Deny a client connection.
     * @param socket socket connection to client
     * @throws Exception
     */
    void rejectClient(Socket socket) throws Exception;

    /**
     * Push dataset to clients.
     * @param acceptedClients list of accepted clients
     * @throws Exception
     */
    void pushDatasetToClients(List<IClientHandler> acceptedClients)
            throws Exception;

    /**
     * Push model to clients.
     * @param acceptedClients list of accepted clients
     * @throws Exception
     */
    void pushModelToClients(List<IClientHandler> acceptedClients)
            throws Exception;

    /**
     * Initiate a training round if certain conditions are met.
     * @param acceptedClients list of accepted clients
     * @param serverParameters given server parameters to verify if
     *                         the conditions for a training round are met
     * @throws Exception
     */
    void trainOrElse(List<IClientHandler> acceptedClients,
                     ServerParameters serverParameters)
            throws Exception;

    /**
     * Aggregate results from clients.
     * @param trainingReports list of training reports
     * @param aggregationStrategy an aggregation strategy
     * @throws Exception
     */
    void aggregateResults(List<TrainingReport> trainingReports,
                          IAggregationStrategy aggregationStrategy)
            throws Exception;

    /**
     * Check if the server is still amidst of a training round.
     * @return <code>true</code> if any client is still training
     * and <code>false</code> otherwise
     */
    Boolean isTraining();
}
