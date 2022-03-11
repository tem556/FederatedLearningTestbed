package com.bnnthang.fltestbed.androidclient;

import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.DataSet;

import java.io.IOException;


/**
 * Imitate Cifar10DataSetIterator and Cifar10Fetcher
 */
public class MyCifar10DataSetIterator extends RecordReaderDataSetIterator {
    private int counter = 0;
    private int numSamples = 0;
    private MyCifar10Loader loader;

    public MyCifar10DataSetIterator(MyCifar10Loader loader, int batchSize,
                                    int labelIndex, int numSamples) {
        super(null, batchSize, labelIndex, 10);
        this.loader = loader;
        this.numSamples = numSamples;
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
            res = loader.createDataSet(num, counter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        counter += num;
        return res;
    }

    @Override
    public boolean hasNext() {
        return counter < numSamples;
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
