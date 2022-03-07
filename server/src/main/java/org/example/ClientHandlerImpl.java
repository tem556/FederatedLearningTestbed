package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.common.primitives.Ints;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class ClientHandlerImpl implements ClientHandler {
    private Socket socket;

    ClientHandlerImpl(Socket socket) throws IOException {
        this.socket = socket;
    }

    @Override
    public void register() throws IOException {
        socket.getOutputStream().write(Ints.toByteArray(0));
        socket.getOutputStream().flush();
    }

    @Override
    public void reject() throws IOException {
        socket.getOutputStream().write(Ints.toByteArray(1));
        socket.getOutputStream().flush();
    }

    @Override
    public INDArray train() throws IOException, InterruptedException {
        socket.getOutputStream().write(Ints.toByteArray(3));
        socket.getOutputStream().flush();
        System.out.println("init training...");

        // push model
        pushModel();

        // poll for update
        int rs = 0;
        do {
            // sleep for 5 secs
            Thread.sleep(5000);

            socket.getOutputStream().write(Ints.toByteArray(4));
            socket.getOutputStream().flush();

            byte[] res = new byte[4];
            socket.getInputStream().read(res);
            rs = Ints.fromByteArray(res);
        } while (rs == 0);

        // get update
        socket.getOutputStream().write(Ints.toByteArray(5));
        socket.getOutputStream().flush();
        int length = Ints.fromByteArray(socket.getInputStream().readNBytes(4));
        byte[] bytes = socket.getInputStream().readNBytes(length);
        return Nd4j.fromByteArray(bytes);
    }

    @Override
    public void pushModel() throws IOException, InterruptedException {
        System.out.println("loading model");

        // send model to client
        File f = new File("C:/Users/buinn/DoNotTouch/crap/photolabeller/newmodel.zip");
        int modelLength = (int)f.length();
        FileInputStream fis = new FileInputStream(f);
        byte[] bytes = new byte[modelLength];
        fis.read(bytes, 0, modelLength);

        System.out.println("pushing");

        socket.getOutputStream().write(Ints.toByteArray(modelLength), 0, 4);
        socket.getOutputStream().flush();
        System.out.println("pushed model length = " + bytes.length);

//        byte[] okBytes = new byte[2];
//        socket.getInputStream().read(okBytes);
//        if (!new String(okBytes, StandardCharsets.US_ASCII).equals("ok")) {
//            throw new IOException("did not receive model length");
//        }

        socket.getOutputStream().write(bytes, 0, modelLength);
        socket.getOutputStream().flush();

        System.out.println("pushed model");
    }

    @Override
    public void done() throws IOException {
        socket.getOutputStream().write(Ints.toByteArray(2));
        socket.getOutputStream().flush();
        socket.close();
    }
}
