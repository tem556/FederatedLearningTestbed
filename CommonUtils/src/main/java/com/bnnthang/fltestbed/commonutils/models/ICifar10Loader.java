package com.bnnthang.fltestbed.commonutils.models;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;

import java.io.IOException;
import java.util.Map;

public interface ICifar10Loader {
    long count() throws IOException;
    Map<Integer, Integer> getDataDistribution() throws IOException;
    DataSet createDataSet(int batchSize, int fromIndex) throws IOException;
    INDArray bytesToImage(byte[] imageBytes) throws IOException;
}
