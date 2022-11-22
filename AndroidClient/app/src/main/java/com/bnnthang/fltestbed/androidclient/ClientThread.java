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

    private int _datasetIndex = 0;

    private File localDir = null;

    public ClientThread(int datasetIndex, File _localDir, String host, int port) {
        localDir = _localDir;
        HOST = host;
        PORT = port;
        _datasetIndex = datasetIndex;
    }

    @Override
    public void run() {
        try {
            IClientLocalRepository localRepository = new AndroidLocalRepository(localDir);
            IClientOperations clientOperations = new AndroidClientOperations(_datasetIndex, localRepository);
            BaseClient client = new BaseClient(HOST, PORT, DELAY_INTERVAL, clientOperations);
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
