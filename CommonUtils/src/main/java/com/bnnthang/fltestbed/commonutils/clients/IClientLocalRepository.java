package com.bnnthang.fltestbed.commonutils.clients;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.common.primitives.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public interface IClientLocalRepository {
    Pair<Long, Long> downloadModel(Socket socket) throws IOException;
    Long downloadDataset(Socket socket) throws IOException;
    Pair<Long, Long> updateModel(Socket socket) throws IOException;
    Boolean modelExists();
    MultiLayerNetwork loadModel() throws IOException;
    Boolean datasetExists();
    Long getDatasetSize() throws IOException;
    InputStream getDatasetInputStream() throws IOException;
    File getModelFile() throws IOException;
    String getModelPath();
    Boolean getUseHealthDataset();
}
