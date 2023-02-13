package com.bnnthang.fltestbed.commonutils.clients;

import java.io.IOException;
import java.time.LocalDateTime;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import com.bnnthang.fltestbed.commonutils.models.ChestXrayDatasetLoader;
import com.bnnthang.fltestbed.commonutils.models.IDatasetLoader;
import com.bnnthang.fltestbed.commonutils.models.MemoryListener;
import com.bnnthang.fltestbed.commonutils.models.ModelUpdate;
import com.bnnthang.fltestbed.commonutils.models.NewChestXrayDSIterator;
import com.bnnthang.fltestbed.commonutils.utils.TimeUtils;

public class ChestXrayTrainingWorker extends Thread {
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
     * Training time update here.
     */
    private IClientTrainingStatManager trainingStatManager;

    /**
     * Constructor for ChestXray training worker.
     * @param localRepository client local repository.
     * @param modelUpdate object to pass the update for IPC.
     * @param batchSize batch size.
     * @param epochs epochs.
     */
    public ChestXrayTrainingWorker(IClientLocalRepository localRepository, ModelUpdate modelUpdate, int batchSize, int epochs, IClientTrainingStatManager trainingStatManager) {
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
            IDatasetLoader loader = new ChestXrayDatasetLoader(localRepository);
            DataSetIterator iterator = new NewChestXrayDSIterator(loader, batchSize);

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
