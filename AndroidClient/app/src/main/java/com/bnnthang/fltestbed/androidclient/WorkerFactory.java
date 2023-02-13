package com.bnnthang.fltestbed.androidclient;

import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import com.bnnthang.fltestbed.commonutils.clients.IClientTrainingStatManager;
import com.bnnthang.fltestbed.commonutils.models.ModelUpdate;

public class WorkerFactory {
    public static Runnable getTrainingWorker(int datasetIndex,
                                             IClientLocalRepository localRepository,
                                             ModelUpdate modelUpdate,
                                             int batchSize,
                                             int epochs,
                                             IClientTrainingStatManager trainingStatManager) {
        switch (datasetIndex) {
            case 0:
                return new AndroidCifar10TrainingWorker(localRepository, modelUpdate, batchSize, epochs, trainingStatManager);
            case 1:
                return new AndroidChestXrayTrainingWorker(localRepository, modelUpdate, batchSize, epochs, trainingStatManager);
            default:
                throw new RuntimeException("unexpected input");
        }
    }
}
