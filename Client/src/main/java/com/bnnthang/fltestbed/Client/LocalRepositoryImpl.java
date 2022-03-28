package com.bnnthang.fltestbed.Client;

import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import com.bnnthang.fltestbed.commonutils.utils.SocketUtils;
import lombok.NonNull;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.*;
import java.net.Socket;

public class LocalRepositoryImpl implements IClientLocalRepository {
    @NonNull
    private final String pathToModel;

    @NonNull
    private final String pathToDataset;

    public LocalRepositoryImpl(@NonNull String _pathToModel, @NonNull String _pathToDataset) {
        pathToModel = _pathToModel;
        pathToDataset = _pathToDataset;
    }

    @Override
    public Long downloadModel(Socket socket) throws IOException {
        File modelFile = new File(pathToModel);

        // create file if not exists
        modelFile.createNewFile();

        // download and write to model file
        FileOutputStream modelFileOutputStream = new FileOutputStream(modelFile);
        Long readBytes = SocketUtils.readAndSaveBytes(socket, modelFileOutputStream);
        modelFileOutputStream.close();

        return readBytes;
    }

    @Override
    public Long updateModel(Socket socket) {
        // TODO: implement this
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Boolean modelExists() {
//        return (new File(pathToModel)).exists();
        return false;
    }

    @Override
    public MultiLayerNetwork loadModel() throws IOException {
        return ModelSerializer.restoreMultiLayerNetwork(pathToModel);
    }

    @Override
    public Long downloadDataset(Socket socket) throws IOException {
        File datasetFile = new File(pathToDataset);

        // create file if not exists
        datasetFile.createNewFile();

        // download and write to model file
        FileOutputStream datasetFileOutputStream = new FileOutputStream(datasetFile);
        Long readBytes = SocketUtils.readAndSaveBytes(socket, datasetFileOutputStream);
        datasetFileOutputStream.close();

        return readBytes;
    }

    @Override
    public Boolean datasetExists() {
        return (new File(pathToDataset)).exists();
    }

    @Override
    public Long getDatasetSize() throws IOException {
        if (datasetExists()) {
            return (new File(pathToDataset)).length();
        } else {
            throw new FileNotFoundException("file does not exist");
        }
    }

    @Override
    public InputStream getDatasetInputStream() throws IOException {
        return new FileInputStream(pathToDataset);
    }


}
