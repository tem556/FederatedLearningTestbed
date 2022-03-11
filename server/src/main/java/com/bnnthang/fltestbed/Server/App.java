package com.bnnthang.fltestbed.Server;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length > 0) {
            ML.trainAndEval();
        } else {
            System.out.println("Hello World!");
            FederatedLearningServer server = new FederatedLearningServerImpl();
            server.startServer();
        }
    }
}
