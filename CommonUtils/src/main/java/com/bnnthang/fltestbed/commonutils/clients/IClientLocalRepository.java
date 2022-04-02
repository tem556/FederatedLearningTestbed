package com.bnnthang.fltestbed.commonutils.clients;

import com.bnnthang.fltestbed.commonutils.models.PowerConsumptionFromBytes;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public interface IClientLocalRepository {
    Long downloadModel(Socket socket) throws IOException;
    Long downloadDataset(Socket socket) throws IOException;
    Long updateModel(Socket socket) throws IOException;
    Long downloadModel(Socket socket, PowerConsumptionFromBytes power) throws IOException;
    Long downloadDataset(Socket socket, PowerConsumptionFromBytes power) throws IOException;
    Long updateModel(Socket socket, PowerConsumptionFromBytes power) throws IOException;
    Boolean modelExists();
    MultiLayerNetwork loadModel() throws IOException;
    Boolean datasetExists();
    Long getDatasetSize() throws IOException;
    InputStream getDatasetInputStream() throws IOException;
}
