package com.bnnthang.fltestbed.androidclient;

import com.bnnthang.fltestbed.clients.BaseClient;
import com.bnnthang.fltestbed.clients.IClientOperations;

import java.io.File;
import java.io.IOException;

public class ClientThread extends Thread {
    public static String host = "10.27.56.216";
    int port = 4602;
    IClientOperations clientOperations = null;
    BaseClient client = null;

    public ClientThread(File localFile) throws IOException {
        clientOperations = new ClientOperations(localFile);
    }

    @Override
    public void run() {
        try {
            client = new BaseClient(host, port, clientOperations);
            client.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
