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
    /**
     * If the Health dataset is to be used.
     */
    private boolean useHealthDataset;

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

            IDatasetLoader loader = new BaseDatasetLoader(localRepository);

            DataSetIterator iterator;
            if (localRepository.getUseHealthDataset()) {
                iterator = new NewChestXrayDSIterator(loader, batchSize);
            }
            else
                iterator = new NewCifar10DSIterator(loader, batchSize);

            model.setListeners(new MemoryListener());

            LocalDateTime startTime = LocalDateTime.now();
            model.fit(iterator, epochs);
            LocalDateTime endTime = LocalDateTime.now();

            report.getMetrics().setTrainingTime(TimeUtils.millisecondsBetween(startTime, endTime));
            report.getModelUpdate().setWeight(model.params().dup());

            model.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
