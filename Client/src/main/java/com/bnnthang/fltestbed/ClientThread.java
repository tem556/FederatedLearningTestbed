package com.bnnthang.fltestbed;

import com.bnnthang.fltestbed.clients.BaseClient;
import com.bnnthang.fltestbed.clients.IClientOperations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public class ClientThread extends Thread {
    String servHost;
    int servPort;
    File localFile;
    IClientOperations clientOperations = null;
    BaseClient client = null;

    public ClientThread(String _servHost, int _servPort, File _localFile) throws IOException {
        servHost = _servHost;
        servPort = _servPort;
        localFile = _localFile;
        clientOperations = new ClientOperations(localFile);
    }

    @Override
    public void run() {
        try {
            client = new BaseClient(servHost, servPort, clientOperations);
            client.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}