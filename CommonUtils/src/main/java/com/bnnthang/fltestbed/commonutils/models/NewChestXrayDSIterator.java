package com.bnnthang.fltestbed.commonutils.models;

import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewChestXrayDSIterator implements DataSetIterator {
    private final IDatasetLoader _loader;

    private int currentIndex = 0;

    private final int _batchSize;

    private Logger _logger = LoggerFactory.getLogger(NewChestXrayDSIterator.class.getName());

    public NewChestXrayDSIterator(IDatasetLoader loader, int batchSize) {
        _loader = loader;
        _batchSize = batchSize;
        _logger.info("init new chest xray dsiterator");
    }

    @Override
    public DataSet next(int numExamples) {
        try {
            DataSet res = _loader.createDataSet(numExamples, currentIndex);
            currentIndex += numExamples;
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int inputColumns() {
        return 3;
    }

    @Override
    public int totalOutcomes() {
        return 2;
    }

    @Override
    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return false;
    }

    @Override
    public void reset() {
        currentIndex = 0;
    }

    @Override
    public int batch() {
        return _batchSize;
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor dataSetPreProcessor) {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public List<String> getLabels() {
        return new ArrayList<>(Arrays.asList(
                "NORMAL",
                "PNEUMONIA"
        ));
    }

    @Override
    public boolean hasNext() {
        try {
            return currentIndex < _loader.count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataSet next() {
        return next(_batchSize);
    }
}
