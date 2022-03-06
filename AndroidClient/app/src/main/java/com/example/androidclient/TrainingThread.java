package com.example.androidclient;

import android.content.res.AssetManager;

import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.IOException;

public class TrainingThread extends Thread {
    public INDArray result;
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

            MyCifar10Loader loader = new MyCifar10Loader(assetManager, DataSetType.TRAIN);
            MyCifar10DataSetIterator cifar = new MyCifar10DataSetIterator(loader, batchSize, 1, samples);

            model.setListeners(new ScoreIterationListener(10));
            model.fit(cifar, epochs);

            // get update from first layer
            result = model.getLayer(0).params();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
