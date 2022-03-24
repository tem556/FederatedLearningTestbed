package com.bnnthang.fltestbed.commonutils.clients;

import com.bnnthang.fltestbed.commonutils.network.SocketUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class BaseClientOperations implements IClientOperations {
    private static final int BATCH_SIZE = 45;
    private static final int EPOCHS = 2;

    private final ILocalRepository localRepository;
    private Cifar10TrainingWorker trainingWorker;

    public BaseClientOperations(ILocalRepository _localRepository) {
        localRepository = _localRepository;
    }

    @Override
    public void handleAccepted(Socket socket) {
        // TODO: send model existence information
    }

    @Override
    public void handleRejected(Socket socket) throws IOException {
        socket.close();
    }

    @Override
    public void handleModelPush(Socket socket) {
        if (localRepository.modelExists()) {
            localRepository.downloadModel(socket);
        } else {
            localRepository.updateModel(socket);
        }
    }

    @Override
    public void handleDatasetPush(Socket socket) {
        localRepository.downloadDataset(socket);
    }

    @Override
    public void handleTrain() {
        trainingWorker = new Cifar10TrainingWorker(localRepository, BATCH_SIZE, EPOCHS);
        trainingWorker.start();
    }

    @Override
    public void handleIsTraining(Socket socket) throws IOException {
        SocketUtils.sendInteger(socket, trainingWorker != null && trainingWorker.isAlive() ? 1 : 0);
    }

    @Override
    public void handleReport(Socket socket) throws IOException {
        // get and convert report to bytes
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(trainingWorker.getReport());
        out.flush();
        byte[] bytes = bos.toByteArray();
        bos.close();

        // send report
        SocketUtils.sendBytesWrapper(socket, bytes);
    }

    @Override
    public void handleDone(Socket socket) throws IOException {
        socket.close();
    }
}
