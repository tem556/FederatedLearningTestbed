package com.bnnthang.fltestbed.commonutils.clients;

import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import com.bnnthang.fltestbed.commonutils.utils.SocketUtils;
import com.bnnthang.fltestbed.commonutils.utils.TimeUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

public class BaseClientOperations implements IClientOperations {
    private static final int BATCH_SIZE = 45;
    private static final int EPOCHS = 2;

    private final IClientLocalRepository localRepository;
    private Cifar10TrainingWorker trainingWorker;

    private TrainingReport trainingReport;

    public BaseClientOperations(IClientLocalRepository _localRepository) {
        localRepository = _localRepository;
        trainingReport = new TrainingReport();
    }

    @Override
    public void handleAccepted(Socket socket) throws IOException {
        // send model existence information
        SocketUtils.sendInteger(socket, hasLocalModel() ? 1 : 0);
    }

    @Override
    public void handleRejected(Socket socket) throws IOException {
        socket.close();
    }

    @Override
    public void handleModelPush(Socket socket) throws IOException {
        LocalDateTime startTime = LocalDateTime.now();
        Long bytesRead = hasLocalModel() ? localRepository.updateModel(socket) : localRepository.downloadModel(socket);
        LocalDateTime endTime = LocalDateTime.now();
        trainingReport.setDownlinkTime(TimeUtils.millisecondsBetween(startTime, endTime) / bytesRead);
    }

    @Override
    public void handleDatasetPush(Socket socket) throws IOException {
        localRepository.downloadDataset(socket);
    }

    @Override
    public void handleTrain() {
        trainingWorker = new Cifar10TrainingWorker(localRepository, trainingReport, BATCH_SIZE, EPOCHS);
        trainingWorker.start();
    }

    @Override
    public void handleIsTraining(Socket socket) throws IOException {
        SocketUtils.sendInteger(socket, trainingWorker != null && trainingWorker.isAlive() ? 1 : 0);
    }

    @Override
    public void handleReport(Socket socket) throws IOException {
        // TODO: handle report better (how?)
        // get and convert report to bytes
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(trainingReport);
        out.flush();
        byte[] bytes = bos.toByteArray();
        bos.close();

        System.out.println("report length = " + bytes.length);

        // send report
        SocketUtils.sendBytesWrapper(socket, bytes);
    }

    @Override
    public void handleDone(Socket socket) throws IOException {
        socket.close();
    }

    @Override
    public Boolean hasLocalModel() {
        return localRepository.modelExists();
    }
}
