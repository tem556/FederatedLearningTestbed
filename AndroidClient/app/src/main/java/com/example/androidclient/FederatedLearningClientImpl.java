package com.example.androidclient;

import android.content.Context;

import org.deeplearning4j.datasets.iterator.impl.Cifar10DataSetIterator;
import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.shade.guava.primitives.Ints;

import java.io.*;
import java.net.Socket;

public class FederatedLearningClientImpl implements FederatedLearningClient {
    private Socket socket;
    private Context appContext;

    FederatedLearningClientImpl(String serverAddr, int serverPort, Context ctx) throws IOException {
        socket = new Socket(serverAddr, serverPort);
        appContext = ctx;
    }

    @Override
    public void register() throws IOException {
        BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        while (true) {
            String s = socketInput.readLine();
            System.out.println(s);
            if (s.equals("registered")) {
                System.out.println("registered");
            } else if (s.equals("rejected")) {
                socket.close();
                System.out.println("rejected");
            } else if (s.equals("closed")) {
                System.out.println("done");
                socketInput.close();
                socket.close();
                break;
            } else if (s.equals("train")) {
                train();
            }
        }
    }

    @Override
    public void train() throws IOException {

//        // get model length
//        byte[] modelLengthBytes = new byte[4];
//        socket.getInputStream().read(modelLengthBytes);
//        int modelLength = Ints.fromByteArray(modelLengthBytes);
//        System.out.println("got model length = " + modelLength);
//
//
//        // get model
//        byte[] bytes = new byte[modelLength];
//        socket.getInputStream().read(bytes);
//
//        // save model to file
//        File path = appContext.getFilesDir();
//        System.out.println(path.getAbsolutePath());
//        File f = new File(path, "testmodel.zip");
//        if (!f.exists()) f.createNewFile();
//        FileOutputStream fos = new FileOutputStream(f);
//        fos.write(bytes);
//        fos.close();
//        System.out.println("saved model");

        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork("model.zip", true);
        System.out.println("loaded model");
//
//        // get ready for training
//        Cifar10DataSetIterator cifar = new Cifar10DataSetIterator(
//                96,
//                new int[] { 32, 32 },
//                DataSetType.TRAIN,
//                null,
//                123L);
//
//        model.setListeners(new ScoreIterationListener(10));
//        model.fit(cifar, 10);
//
//        // get update from layer
//        // reference from mccorby's PhotoLabeller
//        INDArray weights = model.getLayer(3).params();
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        Nd4j.write(outputStream, weights);
//        outputStream.flush();
//        byte[] bytes = outputStream.toByteArray();
//        outputStream.close();
//
//        // send update to server
//        socket.getOutputStream().write(bytes);
    }
}
