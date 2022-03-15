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
     * @param socket some socket
     */
    void handleModelPush(Socket socket);

    /**
     * Handle <code>DATASETPUSH</code> command.
     * @param socket some socket
     */
    void handleDatasetPush(Socket socket);

    /**
     * Handle <code>TRAIN</code> command.
     */
    void handleTrain();

    /**
     * Handle <code>ISTRAINING</code> command.
     * @param socket some socket
     */
    void handleIsTraining(Socket socket);

    /**
     * Handle <code>REPORTED</code> command.
     * @param socket some socket
     */
    void handleReport(Socket socket);

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
