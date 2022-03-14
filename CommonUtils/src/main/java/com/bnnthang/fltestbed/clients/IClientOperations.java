package com.bnnthang.fltestbed.clients;

import java.net.Socket;

/**
 * Required operations that client has to support.
 */
public interface IClientOperations {
    /**
     * Handle <code>ACCEPTED</code> command.
     */
    void handleAccepted();

    /**
     * Handle <code>REJECTED</code> command.
     */
    void handleRejected();

    /**
     * Handle <code>MODELPUSH</code> command.
     */
    void handleModelPush(Socket socket);

    /**
     * Handle <code>DATASETPUSH</code> command.
     */
    void handleDatasetPush(Socket socket);

    /**
     * Handle <code>TRAIN</code> command.
     */
    void handleTrain();

    /**
     * Handle <code>ISTRAINING</code> command.
     */
    void handleIsTraining();

    /**
     * Handle <code>REPORTED</code> command.
     */
    void handleReport();

    /**
     * Handle <code>DONE</code> command.
     */
    void handleDone();

    /**
     * Check if client has model locally.
     * @return <code>true</code> iff client has local model
     */
    Boolean hasLocalModel();
}
