package com.example.androidclient;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.util.Pair;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.util.FeatureUtil;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyCifar10Loader {
    private AssetManager assetManager;
    private List<Pair<byte[], Byte>> imagesWithLabel;

    public MyCifar10Loader(AssetManager assetManager, DataSetType set) throws IOException {
        this.assetManager = assetManager;
        this.imagesWithLabel = new ArrayList<>();

        switch (set) {
            case TRAIN:
                loadTrainData();
                break;
            case TEST:
                loadTestData();
                break;
            default:
                throw new UnsupportedOperationException("unsupported dataset");
        }
    }

    private void loadTrainData() throws IOException {
        imagesWithLabel.clear();
        load("cifar-10/data_batch_1.bin");
        load("cifar-10/data_batch_2.bin");
        load("cifar-10/data_batch_3.bin");
        load("cifar-10/data_batch_4.bin");
        load("cifar-10/data_batch_5.bin");
    }

    private void loadTestData() throws IOException {
        imagesWithLabel.clear();
        load("cifar-10/test_batch.bin");
    }

    @SuppressLint("DefaultLocale")
    private void load(String path) throws IOException {
        InputStream inputStream = assetManager.open(path);
        int imageSize = 32 * 32 * 3;
        int labelSize = 1;
        int rowSize = imageSize + labelSize;
        while (inputStream.available() >= rowSize) {
            byte[] labelBytes = new byte[labelSize];
            byte[] imageBytes = new byte[imageSize];
            int bytesRead = inputStream.read(labelBytes) + inputStream.read(imageBytes);

            if (bytesRead != rowSize) {
                throw new IOException("read invalid row");
            }

            imagesWithLabel.add(new Pair<byte[], Byte>(imageBytes, labelBytes[0]));
        }
    }

    public DataSet createDataSet(int batchSize, int fromIndex) throws IOException {
        if (imagesWithLabel.isEmpty()) return DataSet.empty();

        List<DataSet> atomicDataSet = new ArrayList<>();
        int toIndex = Math.min(imagesWithLabel.size(), fromIndex + batchSize);
        for (int i = fromIndex; i < toIndex; ++i) {
            INDArray image = bytesToImage(imagesWithLabel.get(i).first);
            INDArray label = FeatureUtil.toOutcomeVector(imagesWithLabel.get(i).second, 10);
            atomicDataSet.add(new DataSet(image, label));
        }

        return DataSet.merge(atomicDataSet);
    }

    private INDArray bytesToImage(byte[] imageBytes) throws IOException {
        Mat mat = new Mat(32, 32, CvType.CV_8UC3);
        mat.put(0, 0, imageBytes);

        NativeImageLoader nativeImageLoader = new NativeImageLoader();
        INDArray image = nativeImageLoader.asMatrix(mat);

        return image.reshape(1, 3, 32, 32);
    }
}
