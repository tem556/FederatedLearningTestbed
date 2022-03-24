package com.bnnthang.fltestbed.Client;

import com.bnnthang.fltestbed.clients.IClientOperations;
import com.bnnthang.fltestbed.network.SocketUtils;

import java.io.*;
import java.net.Socket;

public class ClientOperations implements IClientOperations {
    public File localFileDir = null;
    public static String DATASET_FILENAME = "dataset";
    public static String MODEL_FILENAME = "model.zip";
    public TrainingThread trainingThread = null;

    public ClientOperations(File localFileDir) {
        this.localFileDir = localFileDir;
    }

    @Override
    public void handleAccepted() {
//        System.out.println("accepted");
    }

    @Override
    public void handleRejected() {
//        System.out.println("rejected");
    }

    @Override
    public void handleDatasetPush(Socket socket) {
        try {
//            System.out.println("downloading dataset...");
            downloadFile(socket, DATASET_FILENAME);
//            System.out.println("downloaded dataset...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleModelPush(Socket socket) {
        try {
//            System.out.println("downloading model...");
            downloadFile(socket, MODEL_FILENAME);
//            System.out.println("downloaded model");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleDone() {
//        System.out.println("done");
    }

    @Override
    public void handleIsTraining(Socket socket) {
        try {
            SocketUtils.sendInteger(socket, trainingThread != null && trainingThread.isAlive() ? 1 : 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleReport(Socket socket) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(trainingThread.report);
            out.flush();
            byte[] bytes = bos.toByteArray();
//            System.out.println("sending = " + bytes.length);
            SocketUtils.sendBytesWrapper(socket, bytes);
//            System.out.println("report sent");
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleTrain() {
        System.gc();
        trainingThread = new TrainingThread(
                new File(localFileDir, DATASET_FILENAME),
                new File(localFileDir, MODEL_FILENAME),
                40,
                3,
                123456);
        trainingThread.start();
    }

    @Override
    public Boolean hasLocalModel() {
        return false;
    }

    private void downloadFile(Socket socket, String filename) throws Exception {
        // get length
        int length = SocketUtils.readInteger(socket);
//        System.out.println("got file length = " + length);

        // download
        File datasetFile = new File(localFileDir, filename);
        if (!datasetFile.exists()) datasetFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(datasetFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        byte[] buf = new byte[2048];
        int cnt = 0;
        while (cnt < length) {
            int c = socket.getInputStream().read(buf, 0, Math.min(2048, length - cnt));
            cnt += c;
            bos.write(buf, 0, c);
//            System.out.println("downloaded " + cnt);
        }
        bos.close();
        fos.close();
//        System.out.println("saved");
    }
}
