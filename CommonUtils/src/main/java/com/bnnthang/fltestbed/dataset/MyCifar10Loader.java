package com.bnnthang.fltestbed.dataset;

import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.util.FeatureUtil;
import org.opencv.core.Mat;
import org.opencv.core.CvType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MyCifar10Loader {
    private File datasetFile;
    private List<Pair<byte[], Byte>> imagesWithLabel;
    private Map<Byte, Integer> cnt;
    private int maxSamples;

    public MyCifar10Loader(File datasetFile, int maxSamples) throws IOException {
        this.datasetFile = datasetFile;
        this.imagesWithLabel = new ArrayList<>();
        this.maxSamples = maxSamples;
        this.cnt = new HashMap<>();
        load();
        Collections.shuffle(imagesWithLabel);
    }

    public int getSize() {
        return imagesWithLabel.size();
    }

    private void load() throws IOException {
        InputStream inputStream = new FileInputStream(datasetFile);
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

            cnt.put(labelBytes[0], cnt.getOrDefault(labelBytes[0], 0) + 1);

            imagesWithLabel.add(new Pair<>(imageBytes, labelBytes[0]));
        }

        System.out.println("data distribution-------------");
        for (Byte name: cnt.keySet()) {
            String key = name.toString();
            String value = cnt.get(name).toString();
            System.out.println(key + " " + value);
        }
    }

    public DataSet createDataSet(int batchSize, int fromIndex) throws IOException {
        if (imagesWithLabel.isEmpty()) return DataSet.empty();

        List<DataSet> atomicDataSets = new ArrayList<>();
        int toIndex = Math.min(imagesWithLabel.size(), fromIndex + batchSize);
        for (int i = fromIndex; i < toIndex; ++i) {
            INDArray image = bytesToImage(imagesWithLabel.get(i).getFirst());
            INDArray label = FeatureUtil.toOutcomeVector(imagesWithLabel.get(i).getSecond(), 10);
            atomicDataSets.add(new DataSet(image, label));
        }

        return DataSet.merge(atomicDataSets);
    }

    private INDArray bytesToImage(byte[] imageBytes) throws IOException {
        Mat mat = new Mat(32, 32, CvType.CV_8UC3);
        mat.put(0, 0, imageBytes);

        NativeImageLoader nativeImageLoader = new NativeImageLoader();
        INDArray image = nativeImageLoader.asMatrix(mat);

        return image.reshape(1, 3, 32, 32);
    }
}
