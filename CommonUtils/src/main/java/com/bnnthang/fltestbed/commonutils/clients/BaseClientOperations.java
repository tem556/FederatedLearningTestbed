package com.bnnthang.fltestbed.commonutils.clients;

import com.bnnthang.fltestbed.commonutils.models.*;
import com.bnnthang.fltestbed.commonutils.utils.SocketUtils;
import com.bnnthang.fltestbed.commonutils.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

public class BaseClientOperations implements IClientOperations {
    private static final Logger _logger = LoggerFactory.getLogger(BaseClientOperations.class);

    protected static final double AVG_POWER_PER_BYTE = 15.0;

    protected static final double AVG_POWER_PER_MFLOP = 0.009;

    protected static final int BATCH_SIZE = 12;

    protected static final int EPOCHS = 2;

    protected double mflops = 0.0;

    protected final IClientLocalRepository localRepository;

    protected Thread trainingWorker;

    protected TrainingReport trainingReport;

    public BaseClientOperations(IClientLocalRepository _localRepository,
                                Double avgPowerPerByte,
                                Double _mflops) throws IOException {
        localRepository = _localRepository;
        trainingReport = new TrainingReport();
        mflops = _mflops;
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

        long bytesRead;

        if (hasLocalModel()) {
            bytesRead = localRepository.updateModel(socket);
        } else {
            bytesRead = localRepository.downloadModel(socket);
        }

        LocalDateTime endTime = LocalDateTime.now();

        trainingReport.getMetrics().setDownlinkBytes(trainingReport.getMetrics().getDownlinkBytes() + bytesRead);
        trainingReport.getMetrics().setDownlinkTime(trainingReport.getMetrics().getDownlinkTime() + TimeUtils.millisecondsBetween(startTime, endTime));
    }

    @Override
    public void handleDatasetPush(Socket socket) throws IOException {
        _logger.debug("start dataset read");

        LocalDateTime startTime = LocalDateTime.now();
        Long bytesRead = localRepository.downloadDataset(socket);
        LocalDateTime endTime = LocalDateTime.now();

        trainingReport.getMetrics().setDownlinkBytes(trainingReport.getMetrics().getDownlinkBytes() + bytesRead);
        trainingReport.getMetrics().setDownlinkTime(trainingReport.getMetrics().getDownlinkTime() + TimeUtils.millisecondsBetween(startTime, endTime));

        _logger.debug("end dataset read");
    }

    @Override
    public void handleTrain() throws IOException {
        trainingWorker = new Cifar10TrainingWorker(
                localRepository,
                trainingReport,
                BATCH_SIZE,
                EPOCHS);
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

        // send report
        SocketUtils.sendBytesWrapper(socket, bytes);

        trainingReport.getModelUpdate().getWeight().close();

        bos.close();
        out.close();
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
