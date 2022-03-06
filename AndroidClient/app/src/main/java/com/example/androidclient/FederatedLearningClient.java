package com.example.androidclient;

import java.io.File;
import java.io.IOException;

public interface FederatedLearningClient {
    public void serve() throws IOException;
    public void train() throws IOException;
    public File getModel() throws IOException;
    public void trainStatus() throws IOException;
    public void trainResult() throws IOException;
}
