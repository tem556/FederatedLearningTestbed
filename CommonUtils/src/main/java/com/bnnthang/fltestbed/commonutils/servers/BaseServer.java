package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.models.ServerParameters;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Simple implementation of a Federated Learning server.
 */
public class BaseServer extends Thread {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServer.class);

    /**
     * Socket opened for connections.
     */
    @Getter
    private final ServerSocket _serverSocket;

    /**
     * Encapsulated server parameters.
     */
    @Getter
    private final ServerParameters _serverParameters;

    /**
     * Instantiate a <code>BaseServer</code> object.
     * @param serverParameters server parameters
     * @throws IOException if I/O errors happen
     */
    public BaseServer(final ServerParameters serverParameters) throws IOException {
        _serverParameters = serverParameters;
        _serverSocket = new ServerSocket(serverParameters.getPort());
    }

    @Override
    public void run() {
        try {
            serve();
        } catch (Exception e) {
            LOGGER.error("EXCEPTION", e);
        }
    }

    /**
     * Start serving connections.
     * @throws IOException if I/O errors happen
     */
    private void serve() throws IOException {
        IServerOperations serverOperations = _serverParameters.getServerOperations();
        do {
            Socket client = _serverSocket.accept();

            LOGGER.debug("getting client");

            if (serverOperations.isTraining()) {
                // reject client if server is training
                serverOperations.rejectClient(client);
            } else {
                // accept client to training queue if server is not training
                serverOperations.acceptClient(client);
            }
        } while (!serverOperations.trainOrElse(_serverParameters));
    }
}
