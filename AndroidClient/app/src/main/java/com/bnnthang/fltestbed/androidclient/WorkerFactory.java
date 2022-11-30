package com.bnnthang.fltestbed.androidclient;

import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import com.bnnthang.fltestbed.commonutils.models.TrainingReport;

public class WorkerFactory {
    public static Runnable getTrainingWorker(int datasetIndex,
                                             IClientLocalRepository localRepository,
                                             TrainingReport report,
                                             int batchSize,
                                             int epochs) {
        switch (datasetIndex) {
            case 0:
                return new AndroidCifar10TrainingWorker(localRepository, report, batchSize, epochs);
            case 1:
                return new AndroidChestXrayTrainingWorker(localRepository, report, batchSize, epochs);
            default:
                throw new RuntimeException("unexpected input");
        }
    }
}
