package com.bnnthang.fltestbed.androidclient;

import com.bnnthang.fltestbed.commonutils.clients.BaseClient;
import com.bnnthang.fltestbed.commonutils.clients.BaseClientOperations;
import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import com.bnnthang.fltestbed.commonutils.clients.IClientOperations;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.BatchNormalization;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.AdaDelta;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;

public class ClientThread extends Thread {
    private String HOST = null;

    private Integer PORT = null;

    private static final Integer DELAY_INTERVAL = 5000;

    private static final Double AVG_POWER_PER_BYTE = 131201.26909090907;

    private static final Double MFLOPS_PER_ROUND = 15.0;

    private File localDir = null;

    public ClientThread(File _localDir, String host, int port) {
        localDir = _localDir;
        HOST = host;
        PORT = port;
    }

    @Override
    public void run() {
        try {
            IClientLocalRepository localRepository = new AndroidLocalRepository(localDir);
            IClientOperations clientOperations = new AndroidClientOperations(localRepository, AVG_POWER_PER_BYTE, MFLOPS_PER_ROUND);
            BaseClient client = new BaseClient(HOST, PORT, DELAY_INTERVAL, clientOperations);
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int height = 32;
    private static int width = 32;
    private static int channels = 3;
    private static int numLabels = 10;
    private static int batchSize = 96;
    private static long seed = 123L;
    private static int epochs = 4;

    public MultiLayerNetwork getModel()  {
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
}
