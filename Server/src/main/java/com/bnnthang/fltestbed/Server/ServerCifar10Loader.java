package com.bnnthang.fltestbed.Server;

import org.datavec.image.loader.Java2DNativeImageLoader;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.util.FeatureUtil;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerCifar10Loader {
    /**
     * Loaded image array.
     */
    private List<Pair<byte[], Byte>> imagesWithLabel;

    public ServerCifar10Loader(File[] files, Double ratio) throws IOException {
        this.imagesWithLabel = new ArrayList<>();

        // Load all files.
        for (File file : files) {
            load(file);
        }

        // Get a dataset sample.
        getPartialDataset(ratio);
    }

    public int getSize() {
        return imagesWithLabel.size();
    }

    private void load(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        int imageSize = 32 * 32 * 3;
        int labelSize = 1;
        int rowSize = imageSize + labelSize;
        while (inputStream.available() >= rowSize) {
            byte[] labelBytes = new byte[labelSize];
            byte[] imageBytes = new byte[imageSize];
            int bytesRead = inputStream.read(labelBytes) + inputStream.read(imageBytes);

            if (bytesRead != rowSize) {
                inputStream.close();
                throw new IOException("read invalid row");
            }

            imagesWithLabel.add(new Pair<>(imageBytes, labelBytes[0]));
        }
        inputStream.close();
    }

    public void getPartialDataset(double ratio) {
        Map<Byte, List<byte[]>> imagesByLabel = new HashMap<>();
        for (Pair<byte[], Byte> kvp : imagesWithLabel) {
            imagesByLabel.putIfAbsent(kvp.getSecond(), new ArrayList<>());
            imagesByLabel.get(kvp.getSecond()).add(kvp.getFirst());
        }
        imagesWithLabel.clear();
        for (Byte label : imagesByLabel.keySet()) {
            long expectedSize = Math.round(ratio * imagesByLabel.get(label).size());
            for (int i = 0; i < expectedSize; ++i) {
                imagesWithLabel.add(new Pair<>(imagesByLabel.get(label).get(i), label));
            }
        }

        Collections.shuffle(imagesWithLabel);
    }

    public void printDistribution() {
        Map<Byte, Integer> dist = new HashMap<>();
        for (Pair<byte[], Byte> kvp : imagesWithLabel) {
            dist.putIfAbsent(kvp.getSecond(), 0);
            dist.put(kvp.getSecond(), dist.get(kvp.getSecond()) + 1);
        }

        // TODO: replace this with a nicer printing logic
        System.out.println("data distribution--------------");
        System.out.println(dist);
        System.out.println("-------------------------------");
    }

    public DataSet createDataSet(int batchSize, int fromIndex) throws IOException {
        if (imagesWithLabel.isEmpty())
            return DataSet.empty();

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
        int[] rgbImage = new int[1024];
        for (int y = 0; y < 32; ++y) {
            for (int x = 0; x < 32; ++x) {
                int r = 0xFF & imageBytes[y * 32 + x];
                int g = 0xFF & imageBytes[1024 + y * 32 + x];
                int b = 0xFF & imageBytes[2048 + y * 32 + x];
                rgbImage[y * 32 + x] = new Color(r, g, b).getRGB();
            }
        }

        BufferedImage bufferedImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 32; ++y) {
            for (int x = 0; x < 32; ++x) {
                bufferedImage.setRGB(x, y, rgbImage[y * 32 + x]);
            }
        }

        Java2DNativeImageLoader imageLoader = new Java2DNativeImageLoader(32, 32, 3);
        INDArray image = imageLoader.asMatrix(bufferedImage, true);

        return image;
    }
}
