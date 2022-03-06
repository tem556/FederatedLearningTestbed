package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.IOException;

public interface ClientHandler {
    public void register() throws IOException;
    public void reject() throws IOException;
    public void pushModel() throws IOException, InterruptedException;
    public INDArray train() throws IOException, InterruptedException;
    public void close() throws IOException;
}
