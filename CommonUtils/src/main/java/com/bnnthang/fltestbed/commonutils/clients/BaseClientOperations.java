package com.bnnthang.fltestbed.commonutils.clients;

import com.bnnthang.fltestbed.commonutils.models.*;
import com.bnnthang.fltestbed.commonutils.utils.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BaseClientOperations implements IClientOperations {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClientOperations.class);

    /**
     * Batch size as a parameter.
     */
    protected int batchSize;

    /**
     * Epochs to train.
     */
    protected static final int EPOCHS = 2;

    /**
     * Local repository.
     */
    protected final IClientLocalRepository localRepository;

    /**
     * Training thread for management.
     */
    protected Thread trainingWorker;

    /**
     * Round model update communication.
     */
    protected ModelUpdate modelUpdate;

    /**
     * Download stat manager.
     */
    protected IClientNetworkStatManager downloadStat;

    /**
     * Upload stat manager.
     */
    protected IClientNetworkStatManager uploadStat;

    /**
     * Training time manager.
     */
    protected IClientTrainingStatManager trainingStat;

    public BaseClientOperations(IClientLocalRepository _localRepository, Integer _batchSize) throws IOException {
        localRepository = _localRepository;
        modelUpdate = new ModelUpdate();
        downloadStat = new ClientNetworkStatManager();
        uploadStat = new ClientNetworkStatManager();
        trainingStat = new ClientTrainingStatManager();
        batchSize = _batchSize;
    }

    @Override
    public void handleAccepted(Socket socket) throws IOException {
        // initialize first round
        downloadStat.newRound();
        uploadStat.newRound();
        trainingStat.newRound();

        // send model existence information
        SocketUtils.clientTrackedSendInteger(socket, hasLocalModel() ? 1 : 0, uploadStat);
    }

    @Override
    public void handleRejected(Socket socket) throws IOException {
        socket.close();
    }

    @Override
    public void handleModelPush(Socket socket) throws IOException {
        // a pair of (bytes read, elapsed time)
        TimedValue<Long> foo;

        if (hasLocalModel()) {
            LOGGER.debug("start updating model");
            foo = localRepository.updateModel(socket);
            LOGGER.debug("end updating model");
        } else {
            LOGGER.debug("start downloading model");
            foo = localRepository.downloadModel(socket);
            LOGGER.debug("end downloading model");
        }

        // update download stat
        downloadStat.increaseBytes(foo.getValue());
        downloadStat.increaseCommTime(foo.getElapsedTime());
    }

    @Override
    public void handleDatasetPush(Socket socket) throws IOException {
        LOGGER.debug("start dataset read");
        // unaccountable for experiments
        localRepository.downloadDataset(socket);
        LOGGER.debug("end dataset read");
    }

    @Override
    public void handleTrain() throws IOException {
        // TODO: change to factory pattern
        if (localRepository.getDatasetName().equals("ChestXray")){
            trainingWorker = new ChestXrayTrainingWorker(localRepository, modelUpdate, batchSize, EPOCHS, trainingStat);
        } else {
            trainingWorker = new Cifar10TrainingWorker(localRepository, modelUpdate, batchSize, EPOCHS, trainingStat);
        }
        trainingWorker.start();
    }

    @Override
    public void handleIsTraining(Socket socket) throws IOException {
        SocketUtils.clientTrackedSendInteger(socket, trainingWorker != null && trainingWorker.isAlive() ? 1 : 0, uploadStat);
    }

    @Override
    public void handleReport(Socket socket) throws IOException {
        // TODO: handle report better (how?)
        // get and convert report to bytes
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(modelUpdate);
        out.flush();
        byte[] bytes = bos.toByteArray();

        // send report
        SocketUtils.clientTrackedSendBytesWrapper(socket, bytes, uploadStat);

        // clean
        modelUpdate.dispose();

        bos.close();
        out.close();

        // new round here
        downloadStat.newRound();
        uploadStat.newRound();
        trainingStat.newRound();
    }

    @Override
    public void handleDone(Socket socket) throws IOException {
        // serialize and send stats
        SocketUtils.serializeAndSend(socket, downloadStat);
        SocketUtils.serializeAndSend(socket, uploadStat);
        SocketUtils.serializeAndSend(socket, trainingStat);

        // end communication
        socket.close();
    }

    @Override
    public Boolean hasLocalModel() {
        return localRepository.modelExists();
    }

    @Override
    public void terminate() {
        trainingWorker.interrupt();
    }
}
