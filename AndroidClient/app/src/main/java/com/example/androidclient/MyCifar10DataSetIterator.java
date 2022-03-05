package com.example.androidclient;

import android.content.res.AssetManager;

import org.apache.commons.io.FileUtils;
import org.datavec.api.io.filters.RandomPathFilter;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.split.BaseInputSplit;
import org.datavec.image.loader.BaseImageLoader;
import org.datavec.image.transform.ImageTransform;
import org.deeplearning4j.common.resources.ResourceType;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.fetchers.Cifar10Fetcher;
import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.deeplearning4j.datasets.iterator.impl.Cifar10DataSetIterator;
import org.nd4j.common.base.Preconditions;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;

import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.InputSplit;
import org.datavec.image.recordreader.ImageRecordReader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


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
//public class MyCifar10DataSetIterator {
//
//}