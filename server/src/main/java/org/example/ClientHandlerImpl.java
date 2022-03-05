package org.example;

import java.io.*;
import java.net.Socket;

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
    public byte[] train() throws IOException {
        output.println("train");

        // push model
        pushModel();

        // get update
        InputStream input = socket.getInputStream();
        byte[] bytes = IOUtils.toByteArray(input);

        return bytes;
    }

    @Override
    public void pushModel() throws IOException {
        System.out.println("loading model");

        // send model to client
        File f = new File("C:/Users/buinn/DoNotTouch/crap/photolabeller/model.zip");
        FileInputStream fis = new FileInputStream(f);
        byte[] bytes = IOUtils.toByteArray(fis);

        System.out.println("pushing");

//        socket.getOutputStream().write(Ints.toByteArray(bytes.length), 0, 4);
//        socket.getOutputStream().flush();
//        System.out.println("pushed model length = " + bytes.length);
//
//        socket.getOutputStream().write(bytes, 0, bytes.length);
//        socket.getOutputStream().flush();
        System.out.println("pushed model");
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
