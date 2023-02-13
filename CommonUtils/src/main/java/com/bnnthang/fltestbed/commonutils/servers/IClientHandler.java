package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.models.ModelUpdate;

import java.io.IOException;

public interface IClientHandler {
    /**
     * Accept client to training queue.
     */
    void accept() throws IOException;

    /**
     * Deny client connection.
     */
    void reject() throws IOException;

    /**
     * Push dataset to client.
     * @param bytes serialized dataset
     */
    void pushDataset(byte[] bytes) throws IOException;

    /**
     * Push model to client.
     * @param bytes serialized model
     */
    void pushModel(byte[] bytes) throws IOException;

    /**
     * Initiate training process at client.
     */
    void startTraining() throws Exception;

    /**
     * Retrieve training report from client.
     * @return the training report
     */
    ModelUpdate getTrainingReport() throws IOException;

    /**
     * Get the final report and close connections.
     */
    void done() throws Exception;

    /**
     * Check if client is still training.
     * @return <code>true</code> if client is training and
     * <code>false</code> otherwise
     */
    Boolean isTraining();

    /**
     * Check if client can still be contacted
     * @return <code>true</code> iff client is alive
     */
    Boolean isAlive();

    /**
     * Check if the client has a version of the model.
     * @return <code>true</code> iff the client does.
     */
    Boolean hasLocalModel();
}
