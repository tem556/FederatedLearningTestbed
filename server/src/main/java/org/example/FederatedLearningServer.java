package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.IOException;
import java.util.List;

public interface FederatedLearningServer {
    public void startServer() throws IOException, InterruptedException;
}
