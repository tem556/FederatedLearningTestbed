package com.bnnthang.fltestbed.androidclient;

import com.bnnthang.fltestbed.commonutils.clients.BaseClientOperations;
import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;

import java.io.IOException;

public class AndroidClientOperations<T> extends BaseClientOperations {
    protected int datasetIndex = 0;

    /**
     * Small batch size.
     */
    protected int ANDROID_BATCH_SIZE = 16;

    public AndroidClientOperations(int datasetIndex, IClientLocalRepository _localRepository) throws IOException {
        super(_localRepository);
        this.datasetIndex = datasetIndex;
    }

    @Override
    public void handleTrain() {
        trainingWorker = new Thread(WorkerFactory.getTrainingWorker(datasetIndex, localRepository, trainingReport, ANDROID_BATCH_SIZE, EPOCHS));
        trainingWorker.start();
    }
}
