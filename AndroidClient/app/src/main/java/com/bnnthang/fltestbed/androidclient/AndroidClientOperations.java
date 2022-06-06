package com.bnnthang.fltestbed.androidclient;

import com.bnnthang.fltestbed.commonutils.clients.BaseClientOperations;
import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;

import java.io.IOException;

public class AndroidClientOperations extends BaseClientOperations {
    public AndroidClientOperations(IClientLocalRepository _localRepository,
                                   Double avgPowerPerByte,
                                   Double _mflops) throws IOException {
        super(_localRepository, avgPowerPerByte, _mflops);
    }

    @Override
    public void handleTrain() throws IOException {
        trainingWorker = new AndroidCifar10TrainingWorker(
                localRepository,
                trainingReport,
                BATCH_SIZE,
                EPOCHS);
        trainingReport.setComputingPower(mflops * EPOCHS * AVG_POWER_PER_MFLOP);
        trainingWorker.start();
    }
}
