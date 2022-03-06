package org.example;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import com.google.common.primitives.Ints;
import org.apache.commons.io.IOUtils;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.util.ModelSerializer;

public class ClientHandlerImpl implements ClientHandler {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    ClientHandlerImpl(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void register() throws IOException {
        output.println("registered");
    }

    @Override
    public void reject() {
        output.println("rejected");
    }

    @Override
    public byte[] train() throws IOException, InterruptedException {
        output.println("train");

        // push model
        pushModel();

        // get update
        InputStream input = socket.getInputStream();
        byte[] bytes = IOUtils.toByteArray(input);

        return bytes;
    }

    @Override
    public void pushModel() throws IOException, InterruptedException {
        System.out.println("loading model");

//        GZIPInputStream gis = new GZIPInputStream();

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

        byte[] okBytes = new byte[2];
        socket.getInputStream().read(okBytes);
        if (!new String(okBytes, StandardCharsets.US_ASCII).equals("ok")) {
            throw new IOException("did not receive model length");
        }

        socket.getOutputStream().write(bytes, 0, modelLength);
        socket.getOutputStream().flush();

        System.out.println("pushed model");

//        Thread.sleep(30000);
    }

    @Override
    public void close() throws IOException {
        // send message to the client to end connection
        output.println("closed");

        // Close the socket
        input.close();
        output.close();
        socket.close();
    }
}
