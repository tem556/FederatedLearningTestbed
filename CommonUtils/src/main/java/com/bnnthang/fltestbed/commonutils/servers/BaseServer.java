package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.models.ServerParameters;
import lombok.Getter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of a Federated Learning server.
 */
public class BaseServer {
    /**
     * Socket opened for connections.
     */
    @Getter
    private final ServerSocket serverSocket;

    /**
     * Accepted clients waiting for training rounds.
     */
    @Getter
    private final List<IClientHandler> acceptedClients;

    /**
     * Encapsulated server parameters.
     */
    @Getter
    private final ServerParameters serverParameters;

    /**
     * Instantiate a <code>BaseServer</code> object.
     * @param myServerParameters server parameters
     * @throws IOException if I/O errors happen
     */
    public BaseServer(final ServerParameters myServerParameters)
            throws IOException {
        serverParameters = myServerParameters;
        serverSocket = new ServerSocket(myServerParameters.getPort());
        acceptedClients = new ArrayList<>();
    }

    /**
     * Start serving connections.
     * @throws Exception if problems happen when executing
     */
    public void serve() throws Exception {
        do {
            Socket client = serverSocket.accept();

            IServerOperations serverOperations =
                    serverParameters.getServerOperations();

            if (serverOperations.isTraining()) {
                // reject client if server is training
                serverOperations.rejectClient(client);
            } else {
                // accept client to training queue if server is not training
                serverOperations.acceptClient(client, acceptedClients);
            }

            // check if should start training
            serverOperations.trainOrElse(acceptedClients, serverParameters);
        } while (true);
    }
}
