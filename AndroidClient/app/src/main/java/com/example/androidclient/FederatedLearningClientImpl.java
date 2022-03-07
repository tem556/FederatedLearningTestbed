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
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class FederatedLearningClientImpl extends Thread implements FederatedLearningClient {
    private String serverAddr;
    private int serverPort;
    private Context appContext;
    private Socket socket = null;
    private TrainingThread trainingThread = null;

    FederatedLearningClientImpl(String serverAddr, int serverPort, Context ctx) throws IOException {
        this.serverAddr = serverAddr;
        this.serverPort = serverPort;
        this.appContext = ctx;
        this.socket = null;
        this.trainingThread = null;
    }

    @Override
    public void serve() throws IOException {
        this.start();
    }

    @Override
    public boolean isRunning() {
        return isAlive();
    }

    @Override
    public void run() {
        try {
            // register to the server
            register();

            while (socket.isConnected()) {
                // poll for coordination
                if (socket.getInputStream().available() <= 0) {
                    Thread.sleep(5000);
                    continue;
                }

                byte[] bytes = new byte[4];
                socket.getInputStream().read(bytes);

                System.out.println("received code " + Ints.fromByteArray(bytes));
                switch (Ints.fromByteArray(bytes)) {
                    case 0: // registered
                        break;
                    case 1: // rejected
                    case 2: // done
                        socket.close();
                        System.out.println("done");
                        return;
                    case 3: // train
                        train();
                        break;
                    case 4: // trainstatus
                        trainStatus();
                        break;
                    case 5: // trainresult
                        trainResult();
                        break;
                    default:
                        throw new UnsupportedOperationException("invalid verb");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void register() throws IOException {
        this.socket = new Socket(serverAddr, serverPort);
    }

    @Override
    public File getModel() throws IOException {
        // get model length
        byte[] modelLengthBytes = new byte[4];
        socket.getInputStream().read(modelLengthBytes);
        int modelLength = Ints.fromByteArray(modelLengthBytes);
        System.out.println("got model length = " + modelLength);

//        socket.getOutputStream().write("ok".getBytes(StandardCharsets.US_ASCII));
//        socket.getOutputStream().flush();

        // save model to file
        File path = appContext.getFilesDir();
        System.out.println(path.getAbsolutePath());
        File f = new File(path, "testmodel.zip");
        if (!f.exists()) f.createNewFile();
        FileOutputStream fos = new FileOutputStream(f);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        // download model
        byte[] buf = new byte[2048];
        int cnt = 0;
        while (cnt < modelLength) {
            int c = socket.getInputStream().read(buf, 0, Math.min(2048, modelLength - cnt));
            cnt += c;
            bos.write(buf, 0, c);
            System.out.println("model downloaded " + cnt);
        }
        bos.close();
        fos.close();
        System.out.println("saved model");

        assert f.length() == modelLength;

        return f;
    }

    @Override
    public void train() throws IOException {
        File f = getModel();

        trainingThread = new TrainingThread(appContext.getAssets(), f, 10, 1, 1000);
        trainingThread.start();
    }

    public void close() throws IOException {
        socket.close();
        socket = null;
    }

    @Override
    public void trainStatus() throws IOException {
        if (trainingThread == null || trainingThread.isAlive()) {
            socket.getOutputStream().write(Ints.toByteArray(0));
            System.out.println("responded 4 - still training");
        } else {
            socket.getOutputStream().write(Ints.toByteArray(1));
            System.out.println("responded 4 - done");
        }
    }

    @Override
    public void trainResult() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Nd4j.write(outputStream, trainingThread.result);
        outputStream.flush();
        byte[] bytes = outputStream.toByteArray();
        int length = bytes.length;

        // send length
        socket.getOutputStream().write(Ints.toByteArray(length), 0, 4);
        socket.getOutputStream().flush();

        // send result
        socket.getOutputStream().write(bytes, 0, length);
        socket.getOutputStream().flush();
    }
}
