package com.bnnthang.fltestbed.Client;

import com.bnnthang.fltestbed.models.TrainingReport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class TrainingThread extends Thread {
    public TrainingReport report;
    private File datasetFile;
    private File modelFile;
    private int batchSize;
    private int epochs;
    private int samples;

    public TrainingThread(File datasetFile, File modelFile, int batchSize, int epochs, int samples) {
        this.datasetFile = datasetFile;
        this.modelFile = modelFile;
        this.batchSize = batchSize;
        this.epochs = epochs;
        this.samples = samples;
        this.report = null;
    }

    @Override
    public void run() {
        try {
            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(modelFile, true);
//            System.out.println("loaded model");

            MyCifar10Loader loader = new MyCifar10Loader(datasetFile, samples);
            MyCifar10DataSetIterator cifar = new MyCifar10DataSetIterator(loader, batchSize, 1, samples);

//            model.setListeners(new ScoreIterationListener(10));

            LocalDateTime startTime = LocalDateTime.now();
            model.fit(cifar, epochs);
            LocalDateTime endTime = LocalDateTime.now();

            // get layers' weights
//            List<INDArray> layerParams = new ArrayList<>();
//            for (int i = 0; i < model.getnLayers(); ++i) {
//                layerParams.add(model.getLayer(i).params());
//            }

            // summarize result
            report = new TrainingReport(model.gradient(), model.params(), Duration.between(startTime, endTime).getSeconds());

//            System.out.println("wtf");
            ModelSerializer.writeModel(model, modelFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
