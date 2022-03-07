package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.IOException;

public interface ClientHandler {
    void register() throws IOException;
    void reject() throws IOException;
    void pushModel() throws IOException, InterruptedException;
    INDArray train() throws IOException, InterruptedException;
    void done() throws IOException;
}
