package com.bnnthang.fltestbed.androidclient;

import com.bnnthang.fltestbed.commonutils.clients.Cifar10TrainingWorker;
import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import com.bnnthang.fltestbed.commonutils.models.BaseCifar10Loader;
import com.bnnthang.fltestbed.commonutils.models.ICifar10Loader;
import com.bnnthang.fltestbed.commonutils.models.MemoryListener;
import com.bnnthang.fltestbed.commonutils.models.NewCifar10DSIterator;
import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import com.bnnthang.fltestbed.commonutils.utils.TimeUtils;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.IOException;
import java.time.LocalDateTime;

public class AndroidCifar10TrainingWorker extends Thread {
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

    public AndroidCifar10TrainingWorker(IClientLocalRepository _localRepository,
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

            ICifar10Loader loader = new AndroidCifar10Loader(localRepository);
            DataSetIterator cifar = new NewCifar10DSIterator(loader, batchSize);
            model.setListeners(new MemoryListener());

            LocalDateTime startTime = LocalDateTime.now();
            model.fit(cifar, epochs);
            LocalDateTime endTime = LocalDateTime.now();

            _report.getMetrics().setTrainingTime(TimeUtils.millisecondsBetween(startTime, endTime));
            _report.getModelUpdate().setWeight(model.params().dup());


            model.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
