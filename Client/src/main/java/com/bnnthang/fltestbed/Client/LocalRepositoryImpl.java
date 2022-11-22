package com.bnnthang.fltestbed.Client;

import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import com.bnnthang.fltestbed.commonutils.models.PowerConsumptionFromBytes;
import com.bnnthang.fltestbed.commonutils.utils.SocketUtils;
import lombok.NonNull;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.*;
import java.net.Socket;

public class LocalRepositoryImpl implements IClientLocalRepository {
    private static final Logger _logger = LogManager.getLogger(LocalRepositoryImpl.class);

    @NonNull
    private final String pathToModel;

    @NonNull
    private final String pathToDataset;
    @NonNull
    public final boolean useHealthDataset;

    public LocalRepositoryImpl(@NonNull String _pathToModel, @NonNull String _pathToDataset,
                               @NonNull boolean _useHealthDataset) {
        pathToModel = _pathToModel;
        pathToDataset = _pathToDataset;
        useHealthDataset = _useHealthDataset;
    }

    @Override
    public Pair<Long, Long> downloadModel(Socket socket) throws IOException {
        File modelFile = new File(pathToModel);

        // create file if not exists
        modelFile.createNewFile();

        // download and write to model file
        FileOutputStream modelFileOutputStream = new FileOutputStream(modelFile);
        Long readBytes = SocketUtils.readAndSaveBytes(socket, modelFileOutputStream);
        Long configurationEnd = System.currentTimeMillis();
        modelFileOutputStream.close();

        return new Pair<>(readBytes, configurationEnd);
    }

    @Override
    public Pair<Long, Long> updateModel(Socket socket) throws IOException {
        byte[] bytes = SocketUtils.readBytesWrapper(socket);
        Long configurationEnd = System.currentTimeMillis();

        _logger.debug("recv weights length = " + bytes.length);

        INDArray params = SerializationUtils.deserialize(bytes);
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(pathToModel);
        model.setParams(params);
        model.save(new File(pathToModel), true);
        model.close();

        return new Pair<>((long) bytes.length, configurationEnd);
    }

    @Override
    public Boolean modelExists() {
        _logger.debug("path to model = " + pathToModel);

        return (new File(pathToModel)).exists();
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

    @Override
    public File getModelFile() throws IOException {
        return new File(pathToModel);
    }

    @Override
    public String getModelPath() {
        return pathToModel;
    }

    @Override
    public Boolean getUseHealthDataset(){return useHealthDataset;}
}
