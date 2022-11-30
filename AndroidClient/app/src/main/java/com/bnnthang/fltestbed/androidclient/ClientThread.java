package com.bnnthang.fltestbed.androidclient;

import com.bnnthang.fltestbed.commonutils.clients.BaseClient;
import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import com.bnnthang.fltestbed.commonutils.clients.IClientOperations;



import java.io.File;
import java.io.IOException;

public class ClientThread extends Thread {
    private String HOST = null;

    private Integer PORT = null;

    private static final Integer DELAY_INTERVAL = 1000;

    private int _datasetIndex = 0;

    private File localDir = null;

    public ClientThread(int datasetIndex, File _localDir, String host, int port) {
        localDir = _localDir;
        HOST = host;
        PORT = port;
        _datasetIndex = datasetIndex;
    }

    @Override
    public void run() {
        try {
            IClientLocalRepository localRepository = new AndroidLocalRepository(localDir);
            IClientOperations clientOperations = new AndroidClientOperations(_datasetIndex, localRepository);
            BaseClient client = new BaseClient(HOST, PORT, DELAY_INTERVAL, clientOperations);
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
