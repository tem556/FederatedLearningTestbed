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
     * Model update here.
     */
    private ModelUpdate modelUpdate;

    /**
     * Training stat here.
     */
    private IClientTrainingStatManager trainingStatManager;

    public Cifar10TrainingWorker(IClientLocalRepository localRepository, ModelUpdate modelUpdate, int batchSize, int epochs, IClientTrainingStatManager trainingStatManager) {
        this.localRepository = localRepository;
        this.modelUpdate = modelUpdate;
        this.batchSize = batchSize;
        this.epochs = epochs;
        this.trainingStatManager = trainingStatManager;
    }

    @Override
    public void run() {
        try {
            // load model
            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(localRepository.getModelFile(), true);

            // load dataset
            IDatasetLoader loader = new Cifar10DatasetLoader(localRepository);
            DataSetIterator iterator = new NewCifar10DSIterator(loader, batchSize);

            model.setListeners(new MemoryListener());

            // train and time track
            LocalDateTime startTime = LocalDateTime.now();
            model.fit(iterator, epochs);
            LocalDateTime endTime = LocalDateTime.now();

            // update
            modelUpdate.setWeight(model.params().dup());
            trainingStatManager.setTrainingTime(TimeUtils.millisecondsBetween(startTime, endTime));

            model.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
