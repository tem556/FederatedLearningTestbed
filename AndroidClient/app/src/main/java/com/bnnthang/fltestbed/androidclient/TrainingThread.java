package com.bnnthang.fltestbed.androidclient;

import android.content.res.AssetManager;

import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrainingThread extends Thread {
    public TrainResult result;
    private AssetManager assetManager;
    private File modelFile;
    private int batchSize;
    private int epochs;
    private int samples;

    public TrainingThread(AssetManager assetManager, File model, int batchSize, int epochs, int samples) {
        this.result = null;
        this.assetManager = assetManager;
        this.modelFile = model;
        this.batchSize = batchSize;
        this.epochs = epochs;
        this.samples = samples;
    }

    @Override
    public void run() {
        try {
            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(modelFile, true);
            System.out.println("loaded model");
//
//            MyCifar10Loader loader = new MyCifar10Loader(assetManager, DataSetType.TRAIN, samples);
//            MyCifar10DataSetIterator cifar = new MyCifar10DataSetIterator(loader, batchSize, 1, samples);
//
//            model.setListeners(new ScoreIterationListener(10));
//
//            LocalDateTime startTime = LocalDateTime.now();
//            model.fit(cifar, epochs);
//            LocalDateTime endTime = LocalDateTime.now();
//
//            // get layers' weights
//            List<INDArray> layerParams = new ArrayList<>();
//            for (int i = 0; i < model.getnLayers(); ++i) {
//                layerParams.add(model.getLayer(i).params());
//            }
//
//            // summarize result
//            result = new TrainResult(layerParams, Duration.between(startTime, endTime).getSeconds());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
