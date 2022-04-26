package com.bnnthang.fltestbed.androidclient;

import com.bnnthang.fltestbed.commonutils.clients.BaseClient;
import com.bnnthang.fltestbed.commonutils.clients.BaseClientOperations;
import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import com.bnnthang.fltestbed.commonutils.clients.IClientOperations;

import java.io.File;
import java.io.IOException;

public class ClientThread extends Thread {
    private static final String HOST = "10.27.56.36";

    private static final Integer PORT = 4602;

    private static final Integer DELAY_INTERVAL = 5000;

    private static final Double AVG_POWER_PER_BYTE = 131201.26909090907;

    private static final Double MFLOPS_PER_ROUND = 15.0;

    private File localDir = null;

    public ClientThread(File _localDir) {
        localDir = _localDir;
    }

    @Override
    public void run() {
        try {
            IClientLocalRepository localRepository = new AndroidLocalRepository(localDir);
            AndroidCifar10Loader loader = new AndroidCifar10Loader(localRepository);
            IClientOperations clientOperations = new BaseClientOperations(localRepository, AVG_POWER_PER_BYTE, MFLOPS_PER_ROUND, loader, true);
            BaseClient client = new BaseClient(HOST, PORT, DELAY_INTERVAL, clientOperations);
            client.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
