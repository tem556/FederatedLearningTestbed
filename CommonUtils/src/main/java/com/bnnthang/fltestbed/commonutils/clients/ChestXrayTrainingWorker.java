package com.bnnthang.fltestbed.commonutils.clients;

import java.io.IOException;
import java.time.LocalDateTime;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import com.bnnthang.fltestbed.commonutils.models.ChestXrayDatasetLoader;
import com.bnnthang.fltestbed.commonutils.models.IDatasetLoader;
import com.bnnthang.fltestbed.commonutils.models.MemoryListener;
import com.bnnthang.fltestbed.commonutils.models.NewChestXrayDSIterator;
import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import com.bnnthang.fltestbed.commonutils.utils.TimeUtils;

public class ChestXrayTrainingWorker implements Runnable {
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

    public ChestXrayTrainingWorker(IClientLocalRepository _localRepository,
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

            IDatasetLoader loader = new ChestXrayDatasetLoader(localRepository);

            DataSetIterator iterator = new NewChestXrayDSIterator(loader, batchSize);

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
