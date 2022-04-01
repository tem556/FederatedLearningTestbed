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
public class BaseServer extends Thread {
    /**
     * Socket opened for connections.
     */
    @Getter
    private final ServerSocket serverSocket;

    /**
     * Encapsulated server parameters.
     */
    @Getter
    private final ServerParameters serverParameters;

    /**
     * Instantiate a <code>BaseServer</code> object.
     * @param _serverParameters server parameters
     * @throws IOException if I/O errors happen
     */
    public BaseServer(final ServerParameters _serverParameters) throws IOException {
        serverParameters = _serverParameters;
        serverSocket = new ServerSocket(_serverParameters.getPort());
    }

    @Override
    public void run() {
        try {
            serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Start serving connections.
     * @throws IOException if I/O errors happen
     */
    private void serve() throws IOException {
        do {
            Socket client = serverSocket.accept();

            System.out.println("getting client");

            IServerOperations serverOperations =
                    serverParameters.getServerOperations();

            if (serverOperations.isTraining()) {
                System.out.println("reject");
                // reject client if server is training
                serverOperations.rejectClient(client);
            } else {
                System.out.println("accept");

                // accept client to training queue if server is not training
                serverOperations.acceptClient(client);

                // check if should start training
                serverOperations.trainOrElse(serverParameters);
            }
        } while (true);
    }
}
