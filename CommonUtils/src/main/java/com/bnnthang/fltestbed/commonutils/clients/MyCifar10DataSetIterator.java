package com.bnnthang.fltestbed.commonutils.clients;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.DataSet;

import java.io.IOException;
import java.util.Map;

public class MyCifar10DataSetIterator extends RecordReaderDataSetIterator {
    private static final Logger _logger = LogManager.getLogger(MyCifar10DataSetIterator.class);

    private int counter = 0;
    private int numSamples = 0;

    public MyCifar10Loader _loader;

    public MyCifar10DataSetIterator(MyCifar10Loader loader,
                                    int batchSize,
                                    int labelIndex) throws IOException {
        this(loader, batchSize, labelIndex, 123456789);
    }

    public MyCifar10DataSetIterator(MyCifar10Loader loader,
                                    int batchSize,
                                    int labelIndex,
                                    int _numSamples) throws IOException {
        super(null, batchSize, labelIndex, 10);
        _loader = loader;
        numSamples = _numSamples;

        if (_logger.isDebugEnabled()) {
            Map<Integer, Integer> dist = _loader.getDataDistribution();
            _logger.debug("data dist: " + dist);
        }
    }

    @Override
    public void reset() {
        super.reset();
        counter = 0;
    }

    @Override
    public DataSet next(int num) {
        DataSet res = null;
        try {
            res = _loader.createDataSet(num, counter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        counter += num;
        return res;
    }

    @Override
    public boolean hasNext() {
        boolean res = false;
        try {
            res = counter < _loader.count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return false;
    }
}
