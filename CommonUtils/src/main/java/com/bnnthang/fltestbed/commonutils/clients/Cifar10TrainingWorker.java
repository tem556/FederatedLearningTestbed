package com.bnnthang.fltestbed.commonutils.clients;

import com.bnnthang.fltestbed.commonutils.models.BaseCifar10DataSetIterator;
import com.bnnthang.fltestbed.commonutils.models.ICifar10Loader;
import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import com.bnnthang.fltestbed.commonutils.utils.TimeUtils;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.Supplier;

public class Cifar10TrainingWorker<Loader extends ICifar10Loader> extends Thread {
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

    private final Supplier<? extends Loader> _loaderConstructor;

    private Boolean _needToCloseModel;

    public Cifar10TrainingWorker(IClientLocalRepository _localRepository,
                                 TrainingReport _report,
                                 int _batchSize,
                                 int _epochs,
                                 Supplier<? extends Loader> loaderConstructor,
                                 Boolean needToCloseModel) {
        localRepository = _localRepository;
        report = _report;
        batchSize = _batchSize;
        epochs = _epochs;
        _loaderConstructor = loaderConstructor;
        _needToCloseModel = needToCloseModel;
    }

    @Override
    public void run() {
        try {
            ICifar10Loader loader = _loaderConstructor.get();
            BaseCifar10DataSetIterator cifar = new BaseCifar10DataSetIterator(loader, batchSize, 1);

            // load model
            File modelFile = localRepository.getModelFile();
            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(modelFile);

            // run the training and measure the training time
            LocalDateTime startTime = LocalDateTime.now();
            model.addListeners(new ScoreIterationListener());
            Nd4j.getMemoryManager().togglePeriodicGc(false);
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
