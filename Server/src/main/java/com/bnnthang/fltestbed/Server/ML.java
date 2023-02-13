package com.bnnthang.fltestbed.Server;

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
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
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
    /**
     * Logger.
     */
    private static final Logger _logger = LoggerFactory.getLogger(ML.class);

    /**
     * Adopted from <url>https://github.com/deeplearning4j/deeplearning4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/quickstart/modeling/convolution/CIFARClassifier.java</url>.
     * @return sample neural network for CIFAR-10 training.
     */
    public static MultiLayerNetwork getModelCifar10() {
        _logger.info("Building simple convolutional network...");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(5432)
                .updater(new AdaDelta())
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new ConvolutionLayer.Builder().kernelSize(3, 3).stride(1, 1).padding(1, 1)
                        .activation(Activation.LEAKYRELU)
                        .nIn(3).nOut(32).build())
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
                        .nOut(10).build())
                .layer(new BatchNormalization())

                .layer(new SubsamplingLayer.Builder().kernelSize(2, 2).stride(2, 2)
                        .poolingType(SubsamplingLayer.PoolingType.AVG).build())

                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .name("output")
                        .nOut(10)
                        .dropOut(0.8)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutional(32, 32, 3))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        return model;
    }

    public static void trainAndEvalCifar10(Integer rounds, Double datasetratio, String workdir) throws IOException {
        DataSetIterator cifar = new ServerCifar10DataSetIterator(new ServerCifar10Loader(new File[] {
                new File(workdir + "/cifar-10/data_batch_1.bin"), new File(workdir + "/cifar-10/data_batch_2.bin"),
                new File(workdir + "/cifar-10/data_batch_3.bin"), new File(workdir + "/cifar-10/data_batch_4.bin"),
                new File(workdir + "/cifar-10/data_batch_5.bin"), }, datasetratio), 16);
        DataSetIterator cifarEval = new ServerCifar10DataSetIterator(
                new ServerCifar10Loader(new File[] { new File(workdir + "/cifar-10/test_batch.bin") }, 1.0), 16);

        // train model and eval model
        MultiLayerNetwork model = getModelCifar10();
        model.setListeners(new ScoreIterationListener(50),
                new EvaluativeListener(cifarEval, 1, InvocationType.EPOCH_END));
        model.fit(cifar, rounds);

        // Save model
        ModelSerializer.writeModel(model, new File(workdir, "model-"
                + (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime()))
                + ".zip"),
                true);
    }

    /**
     * Adopted from
     * <url>https://www.kaggle.com/code/amyjang/tensorflow-pneumonia-classification-on-x-rays</url>.
     * 
     * @return a sample neural network for Chest Xray/Pneumonia training.
     */
    public static MultiLayerNetwork getModelPneumonia() {
        _logger.info("Building simple convolutional network...");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(5432)
                .updater(new Adam(0.0001))
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new Convolution2D.Builder().convolutionMode(ConvolutionMode.Same)
                        .kernelSize(3, 3)
                        .activation(Activation.RELU).nIn(3).nOut(16).build())
                .layer(new Convolution2D.Builder().convolutionMode(ConvolutionMode.Same)
                        .kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(16).build())
                .layer(new SubsamplingLayer.Builder().kernelSize(2, 2)
                        .poolingType(SubsamplingLayer.PoolingType.MAX).build())

                .layer(new SeparableConvolution2D.Builder().padding(1, 1).kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(32).stride(1, 1).dilation(1, 1)
                        .build())
                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same)
                        .kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(32).build())
                .layer(new BatchNormalization())
                .layer(new Pooling2D.Builder().poolingType(PoolingType.MAX).kernelSize(2, 2).build())

                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same)
                        .kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(64).build())
                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same)
                        .kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(64).build())
                .layer(new BatchNormalization())
                .layer(new Pooling2D.Builder().poolingType(PoolingType.MAX).kernelSize(2, 2).build())

                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same)
                        .kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(128).build())
                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same)
                        .kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(128).build())
                .layer(new BatchNormalization())
                .layer(new Pooling2D.Builder().poolingType(PoolingType.MAX).kernelSize(2, 2).build())

                .layer(new DropoutLayer.Builder().dropOut(0.2).build())

                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same)
                        .kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(256).build())
                .layer(new SeparableConvolution2D.Builder().convolutionMode(ConvolutionMode.Same)
                        .kernelSize(3, 3)
                        .activation(Activation.RELU).nOut(256).build())
                .layer(new BatchNormalization())
                .layer(new Pooling2D.Builder().poolingType(PoolingType.MAX).kernelSize(2, 2).build())

                .layer(new DropoutLayer.Builder().dropOut(0.2).build())
                .layer(new DenseLayer.Builder().nOut(512).activation(Activation.RELU).build())
                .layer(new DenseLayer.Builder().nOut(128).activation(Activation.RELU).build())
                .layer(new DenseLayer.Builder().nOut(64).activation(Activation.RELU).build())

                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .name("output")
                        .nOut(2)
                        .build())
                .setInputType(InputType.convolutional(180, 180, 3))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        return model;
    }

    public static void trainAndEvalPneumonia(Integer rounds, Double datasetratio, String workdir)
            throws IOException {
        DataSetIterator iteratorTrain = new ServerChestXrayDataSetIterator(
                new ServerChestXrayLoader(new File[] { new File(workdir, "train_batch.bin") },
                        datasetratio),
                16);

        DataSetIterator iteratorEval = new ServerChestXrayDataSetIterator(
                new ServerChestXrayLoader(new File[] { new File(workdir, "test_batch.bin") }, 1.0), 16);

        MultiLayerNetwork model = getModelPneumonia();
        model.setListeners(
                new ScoreIterationListener(50),
                new EvaluativeListener(iteratorEval, 1, InvocationType.EPOCH_END));
        model.fit(iteratorTrain, rounds);

        ModelSerializer.writeModel(model, new File(workdir, "cmodel-"
                + (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime()))
                + ".zip"),
                true);
    }

    public static void getBaseCifar10Model(String path) throws IOException {
        File newModel = new File(path, "base_model.zip");
        MultiLayerNetwork model = getModelCifar10();
        model.save(newModel, true);
        model.close();
    }

    public static void getBaseChestXrayModel(String path) throws IOException {
        File newModel = new File(path, "base_model.zip");
        MultiLayerNetwork model = getModelPneumonia();
        model.save(newModel, true);
        model.close();
    }
}
