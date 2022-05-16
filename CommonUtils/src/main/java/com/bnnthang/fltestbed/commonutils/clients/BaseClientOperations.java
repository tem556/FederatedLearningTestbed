package com.bnnthang.fltestbed.commonutils.clients;

import com.bnnthang.fltestbed.commonutils.models.BaseCifar10Loader;
import com.bnnthang.fltestbed.commonutils.models.ICifar10Loader;
import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import com.bnnthang.fltestbed.commonutils.utils.SocketUtils;
import com.bnnthang.fltestbed.commonutils.utils.TimeUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

public class BaseClientOperations implements IClientOperations {
    private static final Logger _logger = LogManager.getLogger(BaseClientOperations.class);

    private static final double AVG_POWER_PER_BYTE = 15.0;

    private static final double AVG_POWER_PER_MFLOP = 0.009;

    private static final int BATCH_SIZE = 12;

    private static final int EPOCHS = 2;

    private double mflops = 0.0;

    private final IClientLocalRepository localRepository;

    private Cifar10TrainingWorker trainingWorker;

    private TrainingReport trainingReport;

    private ICifar10Loader _cifar10Loader;

    public BaseClientOperations(IClientLocalRepository _localRepository,
                                Double avgPowerPerByte,
                                Double _mflops,
                                ICifar10Loader cifar10Loader) throws IOException {
        localRepository = _localRepository;
        trainingReport = new TrainingReport();
        trainingReport.getCommunicationPower().setAvgPowerPerBytes(avgPowerPerByte);
        mflops = _mflops;
        _cifar10Loader = cifar10Loader;
    }

    @Override
    public void handleAccepted(Socket socket) throws IOException {
        // send model existence information
        SocketUtils.sendInteger(socket, hasLocalModel() ? 1 : 0, trainingReport.getCommunicationPower());
    }

    @Override
    public void handleRejected(Socket socket) throws IOException {
        socket.close();
    }

    @Override
    public void handleModelPush(Socket socket) throws IOException {
        LocalDateTime startTime = LocalDateTime.now();
        Long bytesRead = hasLocalModel() ?
                localRepository.updateModel(socket, trainingReport.getCommunicationPower()) :
                localRepository.downloadModel(socket, trainingReport.getCommunicationPower());

        LocalDateTime endTime = LocalDateTime.now();

        trainingReport.setDownlinkTime(TimeUtils.millisecondsBetween(startTime, endTime) / bytesRead);
    }

    @Override
    public void handleDatasetPush(Socket socket) throws IOException {
        _logger.debug("start dataset read");
        localRepository.downloadDataset(socket, trainingReport.getCommunicationPower());
        _logger.debug("end dataset read");
    }

    @Override
    public void handleTrain() throws IOException {
        trainingWorker = new Cifar10TrainingWorker(
                localRepository,
                trainingReport,
                BATCH_SIZE,
                EPOCHS,
                () -> _cifar10Loader);
        trainingReport.setComputingPower(mflops * EPOCHS * AVG_POWER_PER_MFLOP);
        trainingWorker.start();
    }

    @Override
    public void handleIsTraining(Socket socket) throws IOException {
        SocketUtils.sendInteger(socket, trainingWorker != null && trainingWorker.isAlive() ? 1 : 0, trainingReport.getCommunicationPower());
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
        out.close();

        // send report
        SocketUtils.sendBytesWrapper(socket, bytes, trainingReport.getCommunicationPower());

        trainingReport.getParams().close();
        trainingReport.setParams(null);
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
