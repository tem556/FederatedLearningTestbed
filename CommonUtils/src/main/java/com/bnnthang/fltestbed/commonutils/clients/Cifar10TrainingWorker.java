package com.bnnthang.fltestbed.commonutils.clients;

import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import com.bnnthang.fltestbed.commonutils.utils.TimeUtils;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.IOException;
import java.time.LocalDateTime;

public class Cifar10TrainingWorker extends Thread {
    /**
     * An instance of local repository.
     */
    private IClientLocalRepository localRepository;

    /**
     * Batch size.
     */
    private int batchSize;

    /**
     * Number of epochs to train.
     */
    private int epochs;

    /**
     * Training report.
     */
    private TrainingReport report;

    public Cifar10TrainingWorker(IClientLocalRepository _localRepository,
                                 TrainingReport _report,
                                 int _batchSize,
                                 int _epochs) {
        localRepository = _localRepository;
        report = _report;
        batchSize = _batchSize;
        epochs = _epochs;
    }

    @Override
    public void run() {
        try {
            MyCifar10Loader loader = new MyCifar10Loader(localRepository);
            MyCifar10DataSetIterator cifar = new MyCifar10DataSetIterator(loader, batchSize, 1);

            // load model
            MultiLayerNetwork model = localRepository.loadModel();

            // run the training and measure the training time
            LocalDateTime startTime = LocalDateTime.now();
            model.addListeners(new ScoreIterationListener());
            model.fit(cifar, epochs);
            LocalDateTime endTime = LocalDateTime.now();

            // update report
            report.setParams(model.params().dup());
            report.setTrainingTime(TimeUtils.millisecondsBetween(startTime, endTime));

            model.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
