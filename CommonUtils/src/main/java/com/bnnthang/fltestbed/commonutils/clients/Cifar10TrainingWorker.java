package com.bnnthang.fltestbed.commonutils.clients;

import com.bnnthang.fltestbed.commonutils.models.*;
import com.bnnthang.fltestbed.commonutils.utils.TimeUtils;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

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
            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(localRepository.getModelFile(), true);

            ICifar10Loader loader = new BaseCifar10Loader(localRepository);
            DataSetIterator cifar = new NewCifar10DSIterator(loader, batchSize);
            model.setListeners(new MemoryListener());

            LocalDateTime startTime = LocalDateTime.now();
            model.fit(cifar, epochs);
            LocalDateTime endTime = LocalDateTime.now();

            report.setTrainingTime(TimeUtils.millisecondsBetween(startTime, endTime));
            report.setParams(model.params().dup());

            model.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
