package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;

public class ClientTrainThread extends Thread {

    public INDArray weightUpdates;
    private ClientHandler clientHandler;

    ClientTrainThread(ClientHandler clientHandler) {
        super();
        this.clientHandler = clientHandler;
    }

    @Override
    public void run() {
        try {
            weightUpdates = clientHandler.train();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
