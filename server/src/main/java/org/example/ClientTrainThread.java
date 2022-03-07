package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;

public class ClientTrainThread extends Thread {

    public TrainResult trainResult;
    private ClientHandler clientHandler;

    ClientTrainThread(ClientHandler clientHandler) {
        super();
        this.trainResult = null;
        this.clientHandler = clientHandler;
    }

    @Override
    public void run() {
        try {
            trainResult = clientHandler.train();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
