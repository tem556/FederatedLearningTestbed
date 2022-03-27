package com.bnnthang.fltestbed.commonutils.clients;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public interface IClientLocalRepository {
    void downloadModel(Socket socket) throws IOException;
    void updateModel(Socket socket);
    Boolean modelExists();
    MultiLayerNetwork loadModel() throws IOException;
    void downloadDataset(Socket socket) throws IOException;
    Boolean datasetExists();
    Long getDatasetSize() throws IOException;
    InputStream getDatasetInputStream() throws IOException;
}
