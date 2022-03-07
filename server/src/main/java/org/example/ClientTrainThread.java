package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;

public class ClientTrainThread extends Thread {

    public INDArray weightUpdates;
    private ClientHandler clientHandler;
    private int rounds;

    ClientTrainThread(ClientHandler clientHandler, int rounds) {
        super();
        this.clientHandler = clientHandler;
        this.rounds = rounds;
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
