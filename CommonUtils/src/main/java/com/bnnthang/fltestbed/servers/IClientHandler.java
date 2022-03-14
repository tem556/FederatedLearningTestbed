package com.bnnthang.fltestbed.servers;

import com.bnnthang.fltestbed.models.TrainingReport;

public interface IClientHandler {
    /**
     * Push dataset to client.
     * @param bytes serialized dataset
     * @throws Exception
     */
    void pushDataset(byte[] bytes) throws Exception;

    /**
     * Push model to client.
     * @param bytes serialized model
     * @throws Exception if problems happen
     */
    void pushModel(byte[] bytes) throws Exception;

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
     * @throws Exception if problems happen
     */
    void accept() throws Exception;

    /**
     * Deny client connection.
     * @throws Exception if problems happen
     */
    void reject() throws Exception;
}
