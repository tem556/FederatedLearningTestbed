package com.bnnthang.fltestbed.commonutils.clients;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.InputStream;
import java.net.Socket;

public interface ILocalRepository {
    void downloadModel(Socket socket);
    void updateModel(Socket socket);
    Boolean modelExists();
    MultiLayerNetwork loadModel();
    void downloadDataset(Socket socket);
    Boolean datasetExists();
    Long getDatasetSize();
    InputStream getDatasetInputStream();
}
