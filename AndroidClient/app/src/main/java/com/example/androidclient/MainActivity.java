package com.example.androidclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

import org.deeplearning4j.datasets.iterator.impl.Cifar10DataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.AdaDelta;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.BatchNormalization;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.nd4j.linalg.activations.Activation;
import org.datavec.image.loader.CifarLoader;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.inputs.InputType;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.loadLibrary("opencv_java");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startbutton_onclick(View view) throws IOException, URISyntaxException {
        System.out.println("starting");

        // TODO: move this to another thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

//
//        String[] files = getApplicationContext().fileList();
//        for (int i = 0; i < files.length; ++i) {
//            System.out.println(files[i]);
//        }

//        String[] t = getAssets().list("cifar10/train/0/9853_airplane.png");
//        for (String i : t) {
//            System.out.println(i);
//        }

//        InputStream is = getAssets().open("cifar10/train/0/9853_airplane.png");
//        System.out.println(is.available());
//        File f = new File("cifar10/train/0/9853_airplane.png");
//        if (f.exists()) {
//            System.out.println("yay");
//        }

//        InputStream is = getAssets().open();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        String s = reader.readLine();
//        System.out.println(s);

//        InputStream is = getAssets().open("model.zip");
//        int n = 10;
//        byte[] b = new byte[n];
//        is.read(b);
//        for (int i = 0; i < n; ++i)
//            System.out.println((int)b[i]);
//        try {
//            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(is, true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        MultiLayerNetwork model = getModel();
//        System.out.println("flag1");
//        MyCifar10Loader loader = new MyCifar10Loader(getAssets(), DataSetType.TRAIN);
//        MyCifar10DataSetIterator cifar = new MyCifar10DataSetIterator(loader, 10, 1, 1000);
//        System.out.println("flag2");


        // get ready for training
        Cifar10DataSetIterator cifar = new Cifar10DataSetIterator(
                batchSize,
                new int[] { 32, 32 },
                DataSetType.TRAIN,
                null,
                123L);
        Cifar10DataSetIterator cifarEval = new Cifar10DataSetIterator(batchSize, new int[]{height, width}, DataSetType.TEST, null, seed);

//        model.setListeners(new ScoreIterationListener(10));
//        model.fit(cifar, 10);

        System.out.println("trained");

//        FederatedLearningClient client = new FederatedLearningClientImpl("10.27.56.25", 4602, getApplicationContext());
//        client.register();
    }

    private static int height = 32;
    private static int width = 32;
    private static int channels = 3;
    private static int numLabels = CifarLoader.NUM_LABELS;
    private static int batchSize = 10;
    private static long seed = 123L;
    private static int epochs = 4;

    public MultiLayerNetwork getModel() {
//        log.info("Building simple convolutional network...");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123L)
                .updater(new AdaDelta())
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new ConvolutionLayer.Builder().kernelSize(3, 3).stride(1, 1).padding(1, 1).activation(Activation.LEAKYRELU)
                        .nIn(channels).nOut(32).build())
                .layer(new BatchNormalization())
                .layer(new SubsamplingLayer.Builder().kernelSize(2, 2).stride(2, 2).poolingType(SubsamplingLayer.PoolingType.MAX).build())

                .layer(new ConvolutionLayer.Builder().kernelSize(1, 1).stride(1, 1).padding(1, 1).activation(Activation.LEAKYRELU)
                        .nOut(16).build())
                .layer(new BatchNormalization())
                .layer(new ConvolutionLayer.Builder().kernelSize(3, 3).stride(1, 1).padding(1, 1).activation(Activation.LEAKYRELU)
                        .nOut(64).build())
                .layer(new BatchNormalization())
                .layer(new SubsamplingLayer.Builder().kernelSize(2, 2).stride(2, 2).poolingType(SubsamplingLayer.PoolingType.MAX).build())

                .layer(new ConvolutionLayer.Builder().kernelSize(1, 1).stride(1, 1).padding(1, 1).activation(Activation.LEAKYRELU)
                        .nOut(32).build())
                .layer(new BatchNormalization())
                .layer(new ConvolutionLayer.Builder().kernelSize(3, 3).stride(1, 1).padding(1, 1).activation(Activation.LEAKYRELU)
                        .nOut(128).build())
                .layer(new BatchNormalization())
                .layer(new ConvolutionLayer.Builder().kernelSize(1, 1).stride(1, 1).padding(1, 1).activation(Activation.LEAKYRELU)
                        .nOut(64).build())
                .layer(new BatchNormalization())
                .layer(new ConvolutionLayer.Builder().kernelSize(1, 1).stride(1, 1).padding(1, 1).activation(Activation.LEAKYRELU)
                        .nOut(numLabels).build())
                .layer(new BatchNormalization())

                .layer(new SubsamplingLayer.Builder().kernelSize(2, 2).stride(2, 2).poolingType(SubsamplingLayer.PoolingType.AVG).build())

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