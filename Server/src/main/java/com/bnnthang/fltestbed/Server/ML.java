package com.bnnthang.fltestbed.Server;

import org.datavec.image.loader.CifarLoader;
import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.deeplearning4j.datasets.iterator.impl.Cifar10DataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
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
import org.nd4j.evaluation.IEvaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.learning.config.AdaDelta;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ML {
    private static int height = 32;
    private static int width = 32;
    private static int channels = 3;
    private static int numLabels = CifarLoader.NUM_LABELS;
    private static int batchSize = 1234;
    private static long seed = 5432L;
    private static int epochs = 123;

    public static MultiLayerNetwork getModel()  {
//        log.info("Building simple convolutional network...");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new AdaDelta())
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new ConvolutionLayer.Builder().kernelSize(3,3).stride(1,1).padding(1,1).activation(Activation.LEAKYRELU)
                        .nIn(channels).nOut(32).build())
                .layer(new BatchNormalization())
                .layer(new SubsamplingLayer.Builder().kernelSize(2,2).stride(2,2).poolingType(SubsamplingLayer.PoolingType.MAX).build())
                .layer(new ConvolutionLayer.Builder().kernelSize(1,1).stride(1,1).padding(1,1).activation(Activation.LEAKYRELU)
                        .nOut(16).build())
                .layer(new BatchNormalization())
                .layer(new ConvolutionLayer.Builder().kernelSize(3,3).stride(1,1).padding(1,1).activation(Activation.LEAKYRELU)
                        .nOut(64).build())
                .layer(new BatchNormalization())
                .layer(new SubsamplingLayer.Builder().kernelSize(2,2).stride(2,2).poolingType(SubsamplingLayer.PoolingType.MAX).build())

                .layer(new ConvolutionLayer.Builder().kernelSize(1,1).stride(1,1).padding(1,1).activation(Activation.LEAKYRELU)
                        .nOut(32).build())
                .layer(new BatchNormalization())
                .layer(new ConvolutionLayer.Builder().kernelSize(3,3).stride(1,1).padding(1,1).activation(Activation.LEAKYRELU)
                        .nOut(128).build())
                .layer(new BatchNormalization())
                .layer(new ConvolutionLayer.Builder().kernelSize(1,1).stride(1,1).padding(1,1).activation(Activation.LEAKYRELU)
                        .nOut(64).build())
                .layer(new BatchNormalization())
                .layer(new ConvolutionLayer.Builder().kernelSize(1,1).stride(1,1).padding(1,1).activation(Activation.LEAKYRELU)
                        .nOut(numLabels).build())
                .layer(new BatchNormalization())

                .layer(new SubsamplingLayer.Builder().kernelSize(2,2).stride(2,2).poolingType(SubsamplingLayer.PoolingType.AVG).build())

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

    public static void trainAndEval() throws IOException {
        Cifar10DataSetIterator cifar = new Cifar10DataSetIterator(batchSize, new int[]{height, width}, DataSetType.TRAIN, null, seed);
//        Cifar10DataSetIterator cifarEval = new Cifar10DataSetIterator(batchSize, new int[]{height, width}, DataSetType.TEST, null, seed);
        ServerCifar10Loader loader = new ServerCifar10Loader(new File("C:\\Users\\buinn\\Repos\\FederatedLearningTestbed\\Server\\src\\main\\resources\\cifar-10\\test_batch.bin"), 12345);
        ServerCifar10DataSetIterator cifarEval = new ServerCifar10DataSetIterator(loader, 234, 1, 123456);

        //train model and eval model
        MultiLayerNetwork model = getModel();

//        FileWriter writer = new FileWriter("C:\\Users\\buinn\\DoNotTouch\\crap\\testbed\\test1.txt");
//        Map<String, INDArray> t = model.paramTable();
//        for (String key : t.keySet()) {
//            writer.write(key + " -> " + t.get(key) + "\n\n");
//            writer.flush();
//        }
//
//        Gradient g = model.getGradient();
//        INDArray t1;
//        if (g != null) {
//            t1 = g.gradient();
//            writer.write(t1.toStringFull());
//            writer.flush();
//        }
//
//        writer.close();

//        IEvaluation evaluation = model.evaluate(cifarEval);
//        System.out.println(evaluation.stats());
//        System.out.println("====================================");
//        System.out.println("Run training...");
        model.setListeners(new ScoreIterationListener(50),
                new EvaluativeListener(cifarEval, 1, InvocationType.EPOCH_END));
//                new MyListener());

        model.fit(cifar, 34);

//        writer = new FileWriter("C:\\Users\\buinn\\DoNotTouch\\crap\\testbed\\test2.txt");
//        t = model.paramTable();
//        for (String key : t.keySet()) {
//            writer.write(key + " -> " + t.get(key) + "\n\n");
//            writer.flush();
//        }
//
//        g = model.getGradient();
//        if (g != null) {
//            t1 = g.gradient();
//            writer.write(t1.toStringFull());
//            writer.flush();
//        }
//
//        writer.close();
    }

    public static void eval() throws IOException {
//        Cifar10DataSetIterator cifar = new Cifar10DataSetIterator(batchSize, new int[]{height, width}, DataSetType.TRAIN, null, seed);
        Cifar10DataSetIterator cifarEval = new Cifar10DataSetIterator(batchSize, new int[]{height, width}, DataSetType.TEST, null, seed);

        //train model and eval model
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork("C:\\Users\\buinn\\DoNotTouch\\crap\\photolabeller\\newmodel.zip");
        IEvaluation evaluation = model.evaluate(cifarEval);
        System.out.println(evaluation.stats());
//        System.out.println(evaluation.stats());
//        System.out.println("====================================");
//        System.out.println("Run training...");
//        model.setListeners(new ScoreIterationListener(50), new EvaluativeListener(cifarEval, 1, InvocationType.EPOCH_END));

//        model.fit(cifar, epochs);
    }
}
