package com.bnnthang.fltestbed.androidclient;

import com.bnnthang.fltestbed.commonutils.clients.BaseClientOperations;
import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;

import java.io.IOException;

public class AndroidClientOperations<T> extends BaseClientOperations {
    /**
     * Decide which dataset (CIFAR-10 or Chest Xray) to use.
     */
    protected int datasetIndex = 0;

    /**
     * Small batch size.
     */
    protected static final int ANDROID_BATCH_SIZE = 16;

    public AndroidClientOperations(int datasetIndex, IClientLocalRepository _localRepository) throws IOException {
        super(_localRepository, ANDROID_BATCH_SIZE);
        this.datasetIndex = datasetIndex;
    }

    @Override
    public void handleTrain() {
        trainingWorker = new Thread(WorkerFactory.getTrainingWorker(datasetIndex, localRepository, modelUpdate, ANDROID_BATCH_SIZE, EPOCHS, trainingStat));
        trainingWorker.start();
    }
}
