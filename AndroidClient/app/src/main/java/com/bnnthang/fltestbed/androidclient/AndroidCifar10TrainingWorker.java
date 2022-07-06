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
    private IClientLocalRepository _localRepository;

    /**
     * Batch size.
     */
    private int _batchSize;

    /**
     * Number of epochs to train.
     */
    private int _epochs;

    /**
     * Training report.
     */
    private TrainingReport _report;

    public AndroidCifar10TrainingWorker(IClientLocalRepository localRepository,
                                        TrainingReport report,
                                        int batchSize,
                                        int epochs) {
        _localRepository = localRepository;
        _report = report;
        _batchSize = batchSize;
        _epochs = epochs;
    }

    @Override
    public void run() {
        try {
            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(_localRepository.getModelFile(), true);

            ICifar10Loader loader = new AndroidCifar10Loader(_localRepository);
            DataSetIterator cifar = new NewCifar10DSIterator(loader, _batchSize);
            model.setListeners(new MemoryListener());

            LocalDateTime startTime = LocalDateTime.now();
            model.fit(cifar, _epochs);
            LocalDateTime endTime = LocalDateTime.now();

            _report.getMetrics().setTrainingTime(TimeUtils.millisecondsBetween(startTime, endTime));
            _report.getModelUpdate().setWeight(model.params().dup());


            model.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
