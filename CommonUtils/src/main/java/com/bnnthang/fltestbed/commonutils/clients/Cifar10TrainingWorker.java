package com.bnnthang.fltestbed.commonutils.clients;

import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import lombok.Getter;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class Cifar10TrainingWorker extends Thread {
    /**
     * An instance of local repository
     */
    private IClientLocalRepository localRepository;

    /**
     * Batch size
     */
    private int batchSize;

    /**
     * Number of epochs to train
     */
    private int epochs;

    /**
     * Dataset sample size to take
     */
    private int samples;

    @Getter
    private TrainingReport report;

    public Cifar10TrainingWorker(IClientLocalRepository _localRepository,
                                 int _batchSize,
                                 int _epochs) {
        localRepository = _localRepository;
        batchSize = _batchSize;
        epochs = _epochs;
        report = null;
    }

    @Override
    public void run() {
        MyCifar10Loader loader = new MyCifar10Loader(localRepository);
        MyCifar10DataSetIterator cifar =
                new MyCifar10DataSetIterator(loader, batchSize, 1);

        try {
            // load model
            MultiLayerNetwork model = localRepository.loadModel();

            // run the training and measure the training time
            LocalDateTime startTime = LocalDateTime.now();
            model.fit(cifar, epochs);
            LocalDateTime endTime = LocalDateTime.now();

            // update report
            report = new TrainingReport(model.gradient(),
                    model.params(),
                    Duration.between(startTime, endTime).getSeconds());

            // clean memory
            System.gc();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
