package com.bnnthang.fltestbed.androidclient;

import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import com.bnnthang.fltestbed.commonutils.clients.IClientTrainingStatManager;
import com.bnnthang.fltestbed.commonutils.models.IDatasetLoader;
import com.bnnthang.fltestbed.commonutils.models.MemoryListener;
import com.bnnthang.fltestbed.commonutils.models.ModelUpdate;
import com.bnnthang.fltestbed.commonutils.models.NewChestXrayDSIterator;
import com.bnnthang.fltestbed.commonutils.utils.TimeUtils;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.IOException;
import java.time.LocalDateTime;

public class AndroidChestXrayTrainingWorker implements Runnable {
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
    private ModelUpdate _modelUpdate;

    /**
     * Training stat here.
     */
    private IClientTrainingStatManager _trainingStatManager;

    public AndroidChestXrayTrainingWorker(IClientLocalRepository localRepository,
                                          ModelUpdate modelUpdate,
                                          int batchSize,
                                          int epochs,
                                          IClientTrainingStatManager trainingStatManager) {
        _localRepository = localRepository;
        _modelUpdate = modelUpdate;
        _batchSize = batchSize;
        _epochs = epochs;
        _trainingStatManager = trainingStatManager;
    }

    @Override
    public void run() {
        try {
            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(_localRepository.getModelFile(), true);

            IDatasetLoader loader = new AndroidChestXrayLoader(_localRepository);
            DataSetIterator chestXray = new NewChestXrayDSIterator(loader, _batchSize);
            model.setListeners(new MemoryListener());

            LocalDateTime startTime = LocalDateTime.now();
            model.fit(chestXray, _epochs);
            LocalDateTime endTime = LocalDateTime.now();

            _modelUpdate.setWeight(model.params().dup());
            _trainingStatManager.setTrainingTime(TimeUtils.millisecondsBetween(startTime, endTime));

            model.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
