package com.bnnthang.fltestbed.clients;

import com.bnnthang.fltestbed.enums.ClientCommandEnum;
import com.bnnthang.fltestbed.network.SocketUtils;

import java.io.IOException;
import java.net.Socket;

/**
 * Simple implementation of a Federated Learning client.
 */
public class BaseClient {
    /**
     * Delay interval (in milliseconds).
     */
    // TODO: make this a variable
    private static final Integer DELAY_INTERVAL = 5000;

    /**
     * Socket connection to server.
     */
    private Socket socket;

    /**
     * Supported client operations.
     */
    private IClientOperations operations;

    /**
     * Constructor for <code>BaseClient</code>.
     * @param host address to connect
     * @param port port to connect
     * @param clientOperations implementation of supported operations
     * @throws IOException
     */
    public BaseClient(final String host,
                      final Integer port,
                      final IClientOperations clientOperations)
            throws IOException {
        socket = new Socket(host, port);
        operations = clientOperations;
    }

    /**
     * Start serving instructions from server.
     * @throws Exception
     */
    public void serve() throws Exception {
        // close connection if rejected
        if (!acceptedOrRejected()) {
            operations.handleDone();
            return;
        }

        do {
            System.out.println("polling...");

            // skip if there is nothing to read
            if (!SocketUtils.availableToRead(socket)) {
                System.out.println("sleeping...");
                Thread.sleep(DELAY_INTERVAL);
                continue;
            }

            coordinate(SocketUtils.readInteger(socket));
        } while (socket.isConnected());
    }

    /**
     * Branch processing based on server instruction.
     * @param commandIndex integer denotes a command enum
     * @throws Exception
     */
    private void coordinate(final Integer commandIndex) throws Exception {
        System.out.println("read command = " + commandIndex);
        if (commandIndex < 0 || commandIndex > ClientCommandEnum.values().length) {
            // TODO: investigate why there is negative command
            return;
        }
        switch (ClientCommandEnum.values()[commandIndex]) {
            case ACCEPTED:
            case REJECTED:
                throw new Exception("unexpected command");
            case MODELPUSH:
                operations.handleModelPush(socket);
                break;
            case DATASETPUSH:
                operations.handleDatasetPush(socket);
                break;
            case TRAIN:
                operations.handleTrain();
                break;
            case ISTRAINING:
                operations.handleIsTraining(socket);
                break;
            case REPORT:
                operations.handleReport(socket);
                break;
            case DONE:
                operations.handleDone();
                break;
            default:
                throw new Exception("unrecognized command");
        }
    }

    /**
     * Check if server rejects connection.
     * @return <code>true</code> iff client is moved to training queue
     * @throws Exception
     */
    private Boolean acceptedOrRejected() throws Exception {
        switch (SocketUtils.readInteger(socket)) {
            case 0:
                SocketUtils.sendInteger(socket,
                        operations.hasLocalModel() ? 1 : 0);
                operations.handleAccepted();
                return true;
            case 1:
                operations.handleRejected();
                return false;
            default:
                throw new Exception("unrecognized command");
        }
    }
}
