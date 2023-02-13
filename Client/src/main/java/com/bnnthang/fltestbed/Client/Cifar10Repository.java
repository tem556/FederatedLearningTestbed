package com.bnnthang.fltestbed.Client;

import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import com.bnnthang.fltestbed.commonutils.models.TimedValue;
import com.bnnthang.fltestbed.commonutils.utils.SocketUtils;
import lombok.NonNull;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Cifar10Repository implements IClientLocalRepository {
    private static final Logger _logger = LogManager.getLogger(Cifar10Repository.class);

    @NonNull
    private final String pathToModel;

    @NonNull
    private final String pathToDataset;

    public Cifar10Repository(@NonNull String _pathToModel, @NonNull String _pathToDataset) {
        pathToModel = _pathToModel;
        pathToDataset = _pathToDataset;
    }

    @Override
    public TimedValue<Long> downloadModel(Socket socket) throws IOException {
        File modelFile = new File(pathToModel);

        // create file if not exists
        modelFile.createNewFile();

        // download and write to model file
        FileOutputStream modelFileOutputStream = new FileOutputStream(modelFile);
        TimedValue<Long> foo = SocketUtils.readAndSaveBytes(socket, modelFileOutputStream);
        modelFileOutputStream.close();

        return foo;
    }

    @Override
    public TimedValue<Long> updateModel(Socket socket) throws IOException {
        TimedValue<byte[]> foo = SocketUtils.readBytesWrapper(socket);

        _logger.debug("recv weights length = " + foo.getValue().length);

        INDArray params = SerializationUtils.deserialize(foo.getValue());
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(pathToModel);
        model.setParams(params);
        model.save(new File(pathToModel), true);
        model.close();

        return new TimedValue<Long>((long) foo.getValue().length, foo.getElapsedTime());
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
        Long readBytes = SocketUtils.readAndSaveBytes(socket, datasetFileOutputStream).getValue();
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
    public String getDatasetName() { return "Cifar10"; }
}
