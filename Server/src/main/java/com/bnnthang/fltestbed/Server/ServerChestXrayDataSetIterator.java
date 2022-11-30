package com.bnnthang.fltestbed.Server;

import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.DataSet;

import java.io.IOException;

public class ServerChestXrayDataSetIterator extends RecordReaderDataSetIterator {
    private int counter = 0;
    public ServerChestXrayLoader loader;

    public ServerChestXrayDataSetIterator(ServerChestXrayLoader _loader, int batchSize) {
        super(null, batchSize, 1, 2);
        loader = _loader;
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
        return counter < loader.getSize();
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
