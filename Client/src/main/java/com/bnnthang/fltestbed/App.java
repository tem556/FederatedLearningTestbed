package com.bnnthang.fltestbed;

import com.bnnthang.fltestbed.servers.IClientHandler;
import com.sun.tools.javac.Main;
import org.opencv.core.Core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class App {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("hello");

        // load properties
        Properties configuration = new Properties();
        configuration.load(Main.class.getClassLoader().getResourceAsStream("config.properties"));
        String servHost = configuration.getProperty("server.host");
        int servPort = Integer.parseInt(configuration.getProperty("server.port"));
        String workDir = configuration.getProperty("workDir");

        System.out.println(servHost);
        System.out.println(servPort);
        System.out.println(workDir);

        if (args.length == 0) {
            System.out.println("missing expected number of clients");
            return;
        }

        int clients = Integer.parseInt(args[0]);
        List<ClientThread> clientPool = new ArrayList<>();
        for (int i = 0; i < clients; ++i) {
            // make client dir if needed
            String clientDir = workDir + "/dirclient" + i;
            Path path = Path.of(clientDir);
            if (Files.notExists(path))
                Files.createDirectory(path);
            File file = new File(clientDir);
            ClientThread clientThread = new ClientThread(servHost, servPort, file);
            clientThread.start();
            clientPool.add(clientThread);
        }
        for (ClientThread thread : clientPool) {
            thread.join();
        }
    }
}
