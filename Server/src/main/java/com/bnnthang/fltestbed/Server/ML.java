package com.bnnthang.fltestbed.Server;

import org.datavec.image.loader.CifarLoader;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.AdaDelta;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ML {
    private static final Logger _logger = LoggerFactory.getLogger(ML.class);
    private static int height = 32;
    private static int width = 32;
    private static int channels = 3;
    private static int numLabels = CifarLoader.NUM_LABELS;
    private static int batchSize = 96;
    private static long seed = 5432L;
    // private static int epochs = 123;

    public static MultiLayerNetwork getModelCifar10() {
        _logger.info("Building simple convolutional network...");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new AdaDelta())
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new ConvolutionLayer.Builder().kernelSize(3, 3).stride(1, 1).padding(1, 1)
                        .activation(Activation.LEAKYRELU)
                        .nIn(channels).nOut(32).build())
                .layer(new BatchNormalization())
                .layer(new SubsamplingLayer.Builder().kernelSize(2, 2).stride(2, 2)
                        .poolingType(SubsamplingLayer.PoolingType.MAX).build())
                .layer(new ConvolutionLayer.Builder().kernelSize(1, 1).stride(1, 1).padding(1, 1)
                        .activation(Activation.LEAKYRELU)
                        .nOut(16).build())
                .layer(new BatchNormalization())
                .layer(new ConvolutionLayer.Builder().kernelSize(3, 3).stride(1, 1).padding(1, 1)
                        .activation(Activation.LEAKYRELU)
                        .nOut(64).build())
                .layer(new BatchNormalization())
                .layer(new SubsamplingLayer.Builder().kernelSize(2, 2).stride(2, 2)
                        .poolingType(SubsamplingLayer.PoolingType.MAX).build())

                .layer(new ConvolutionLayer.Builder().kernelSize(1, 1).stride(1, 1).padding(1, 1)
                        .activation(Activation.LEAKYRELU)
                        .nOut(32).build())
                .layer(new BatchNormalization())
                .layer(new ConvolutionLayer.Builder().kernelSize(3, 3).stride(1, 1).padding(1, 1)
                        .activation(Activation.LEAKYRELU)
                        .nOut(128).build())
                .layer(new BatchNormalization())
                .layer(new ConvolutionLayer.Builder().kernelSize(1, 1).stride(1, 1).padding(1, 1)
                        .activation(Activation.LEAKYRELU)
                        .nOut(64).build())
                .layer(new BatchNormalization())
                .layer(new ConvolutionLayer.Builder().kernelSize(1, 1).stride(1, 1).padding(1, 1)
                        .activation(Activation.LEAKYRELU)
                        .nOut(numLabels).build())
                .layer(new BatchNormalization())

                .layer(new SubsamplingLayer.Builder().kernelSize(2, 2).stride(2, 2)
                        .poolingType(SubsamplingLayer.PoolingType.AVG).build())

                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .name("output")
                        .nOut(numLabels)
                        .dropOut(0.8)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutional(height, width, channels))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        return model;
    }

    public static void trainAndEvalCifar10(Integer rounds, Float datasetratio, String workdir) throws IOException {
        ServerCifar10Loader loaderTrain = new ServerCifar10Loader(new File[] {
                new File(workdir + "/cifar-10/data_batch_1.bin"),
                new File(workdir + "/cifar-10/data_batch_2.bin"),
                new File(workdir + "/cifar-10/data_batch_3.bin"),
                new File(workdir + "/cifar-10/data_batch_4.bin"),
                new File(workdir + "/cifar-10/data_batch_5.bin"),
        });
        loaderTrain.getPartialDataset(datasetratio);
        loaderTrain.printDistribution();

        ServerCifar10DataSetIterator cifar = new ServerCifar10DataSetIterator(loaderTrain, batchSize, 1, 123456);

        ServerCifar10Loader loader = new ServerCifar10Loader(new File(workdir + "/cifar-10/test_batch.bin"), 12345);
        loader.printDistribution();
        ServerCifar10DataSetIterator cifarEval = new ServerCifar10DataSetIterator(loader, batchSize, 1, 123456);

        // train model and eval model
        MultiLayerNetwork model = getModelCifar10();

        model.setListeners(new ScoreIterationListener(50),
                new EvaluativeListener(cifarEval, 1, InvocationType.EPOCH_END));

        model.fit(cifar, rounds);
        ModelSerializer.writeModel(model, new File(workdir, "model-"
                + (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime())) + ".zip"),
                true);
    }

    public static MultiLayerNetwork getModelPneumonia() {
        _logger.info("Building simple convolutional network...");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new Adam(0.0001))
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new Convolution2D.Builder().convolutionMode(ConvolutionMode.Same).kernelSize(3, 3)
                        .activation(Activation.RELU).nIn(channels).nOut(16).build())
                .layer(new Convolution2D.Builder().convolutionMode(ConvolutionMode.Same).kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(16).build())
                .layer(new Pooling2D.Builder().poolingType(PoolingType.MAX).kernelSize(2, 2).build())

                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same).kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(32).build())
                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same).kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(32).build())
                .layer(new BatchNormalization())
                .layer(new Pooling2D.Builder().poolingType(PoolingType.MAX).kernelSize(2, 2).build())

                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same).kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(64).build())
                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same).kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(64).build())
                .layer(new BatchNormalization())
                .layer(new Pooling2D.Builder().poolingType(PoolingType.MAX).kernelSize(2, 2).build())

                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same).kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(128).build())
                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same).kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(128).build())
                .layer(new BatchNormalization())
                .layer(new Pooling2D.Builder().poolingType(PoolingType.MAX).kernelSize(2, 2).build())

                .layer(new DropoutLayer.Builder().dropOut(0.2).build())

                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same).kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(256).build())
                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same).kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(256).build())
                .layer(new BatchNormalization())
                .layer(new Pooling2D.Builder().poolingType(PoolingType.MAX).kernelSize(2, 2).build())

                .layer(new DropoutLayer.Builder().dropOut(0.2).build())

                .layer(new DenseLayer.Builder().nOut(512).activation(Activation.RELU).build())
                .layer(new DenseLayer.Builder().nOut(128).activation(Activation.RELU).build())
                .layer(new DenseLayer.Builder().nOut(64).activation(Activation.RELU).build())
                .layer(new DenseLayer.Builder().nOut(2).activation(Activation.SIGMOID).build())

                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.HINGE)
                        .name("output")
                        .nOut(2)
                        .build())
                .setInputType(InputType.convolutional(180, 180, 3))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        return model;
    }

    public static void trainAndEvalPneumonia(Integer rounds, Float datasetratio, String workdir) throws IOException {
        ServerChestXrayLoader loaderTrain = new ServerChestXrayLoader(
                new File(workdir, "train_batch.bin"));
        ServerChestXrayDataSetIterator iteratorTrain = new ServerChestXrayDataSetIterator(loaderTrain, batchSize, 1, 1234567);
        
        ServerChestXrayLoader loaderEval = new ServerChestXrayLoader(
                new File(workdir, "test_batch.bin"));
        ServerChestXrayDataSetIterator iteratorEval = new ServerChestXrayDataSetIterator(loaderEval, batchSize, 1, 1234567);
        
        MultiLayerNetwork model = getModelPneumonia();
        model.setListeners(new ScoreIterationListener(50),
                new EvaluativeListener(iteratorEval, 1, InvocationType.EPOCH_END));

        model.fit(iteratorTrain, rounds);
        ModelSerializer.writeModel(model, new File(workdir, "cmodel-"
                + (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime())) + ".zip"),
                true);   
    }
}
