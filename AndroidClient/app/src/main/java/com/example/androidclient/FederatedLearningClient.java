package com.example.androidclient;

import java.io.File;
import java.io.IOException;

public interface FederatedLearningClient {
    void serve() throws IOException;
    void register() throws IOException;
    void train() throws IOException;
    File getModel() throws IOException;
    void trainStatus() throws IOException;
    void trainResult() throws IOException;
    boolean isRunning();
}
