package com.bnnthang.fltestbed.Server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.google.common.primitives.Ints;
import com.google.gson.Gson;

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
        System.out.println("rejected a device");
        socket.getOutputStream().write(Ints.toByteArray(1));
        socket.getOutputStream().flush();
    }

    @Override
    public TrainResult train() throws IOException, InterruptedException {
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

        // get train result
        socket.getOutputStream().write(Ints.toByteArray(5));
        socket.getOutputStream().flush();
        int length = Ints.fromByteArray(socket.getInputStream().readNBytes(4));
        byte[] bytes = socket.getInputStream().readNBytes(length);
        String json = new String(bytes, StandardCharsets.US_ASCII);
        return new Gson().fromJson(json, TrainResult.class);
    }

    @Override
    public void pushModel() throws IOException, InterruptedException {
        System.out.println("loading model");

        // send model to client
        // TODO: change the hard-coded value
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
