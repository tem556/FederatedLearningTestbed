package com.bnnthang.fltestbed.servers;

import com.bnnthang.fltestbed.models.TrainingReport;

import java.net.Socket;

public interface IClientHandler {
    /**
     * Push dataset to client.
     * @throws Exception if problems happen
     */
    void pushDataset() throws Exception;

    /**
     * Push model to client.
     * @throws Exception if problems happen
     */
    void pushModel() throws Exception;

    /**
     * Initiate training process at client.
     * @throws Exception if problems happen
     */
    void startTraining() throws Exception;

    /**
     * Retrieve training report from client.
     * @return the training report
     */
    TrainingReport getTrainingReport();

    /**
     * Close connection.
     * @throws Exception if problems happen
     */
    void done() throws Exception;

    /**
     * Check if client is still training.
     * @return <code>true</code> if client is training and
     * <code>false</code> otherwise
     */
    Boolean isTraining();

    /**
     * Accept client to training queue.
     * @param socket socket connection to client
     * @return a <code>IClientHandler</code> based on the given socket
     * @throws Exception if problems happen
     */
    IClientHandler accept(Socket socket) throws Exception;

    /**
     * Deny client connection.
     * @param socket socket connection to client
     * @throws Exception if problems happen
     */
    void reject(Socket socket) throws Exception;
}
