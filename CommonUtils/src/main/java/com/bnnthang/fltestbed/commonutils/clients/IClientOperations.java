package com.bnnthang.fltestbed.commonutils.clients;

import java.io.IOException;
import java.net.Socket;

/**
 * Required operations that client has to support.
 */
public interface IClientOperations {
    /**
     * Handle <code>ACCEPTED</code> command.
     */
    void handleAccepted(Socket socket);

    /**
     * Handle <code>REJECTED</code> command.
     */
    void handleRejected(Socket socket) throws IOException;

    /**
     * Handle <code>MODELPUSH</code> command.
     * @param socket some socket
     */
    void handleModelPush(Socket socket) throws IOException;

    /**
     * Handle <code>DATASETPUSH</code> command.
     * @param socket some socket
     */
    void handleDatasetPush(Socket socket) throws IOException;

    /**
     * Handle <code>TRAIN</code> command.
     */
    void handleTrain();

    /**
     * Handle <code>ISTRAINING</code> command.
     * @param socket some socket
     */
    void handleIsTraining(Socket socket) throws IOException;

    /**
     * Handle <code>REPORTED</code> command.
     * @param socket some socket
     */
    void handleReport(Socket socket) throws IOException;

    /**
     * Handle <code>DONE</code> command.
     */
    void handleDone(Socket socket) throws IOException;

    /**
     * Check if client has model locally.
     * @return <code>true</code> iff client has local model
     */
    Boolean hasLocalModel();
}
