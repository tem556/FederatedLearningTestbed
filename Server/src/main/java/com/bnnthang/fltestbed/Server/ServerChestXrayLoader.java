package com.bnnthang.fltestbed.Server;

import org.datavec.image.loader.Java2DNativeImageLoader;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.util.FeatureUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ServerChestXrayLoader {
    /**
     * Label size (in bytes).
     */
    protected final static int LABEL_SIZE = 1;

    /**
     * Image height (in pixels).
     */
    protected static int IMAGE_HEIGHT = 180;

    /**
     * Image width (in pixels).
     */
    protected static int IMAGE_WIDTH = 180;

    /**
     * Image channels.
     */
    protected static int IMAGE_CHANNELS = 3;

    /**
     * Image size (in bytes).
     */
    protected static int IMAGE_SIZE = IMAGE_WIDTH * IMAGE_HEIGHT * IMAGE_CHANNELS;

    /**
     * Row size (in bytes).
     */
    protected static int ROW_SIZE = LABEL_SIZE + IMAGE_SIZE;

    private List<Pair<byte[], Byte>> imagesWithLabel;

    public ServerChestXrayLoader(File datasetFile) throws IOException {
        this.imagesWithLabel = new ArrayList<>();
        load(datasetFile);
    }

    private void load(File file) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        int imageSize = IMAGE_HEIGHT * IMAGE_WIDTH * IMAGE_CHANNELS;
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
            INDArray label = FeatureUtil.toOutcomeVector(imagesWithLabel.get(i).getSecond(), 2);
            atomicDataSets.add(new DataSet(image, label));
        }

        return DataSet.merge(atomicDataSets);
    }

    private INDArray bytesToImage(byte[] imageBytes) throws IOException {
        int[] rgbImage = new int[IMAGE_HEIGHT * IMAGE_WIDTH];
        for (int y = 0; y < IMAGE_HEIGHT; ++y) {
            for (int x = 0; x < IMAGE_WIDTH; ++x) {
                int r = 0xFF & imageBytes[y * IMAGE_WIDTH + x];
                int g = 0xFF & imageBytes[IMAGE_HEIGHT * IMAGE_WIDTH + y * IMAGE_WIDTH + x];
                int b = 0xFF & imageBytes[2 * IMAGE_HEIGHT * IMAGE_WIDTH + y * IMAGE_WIDTH + x];
                rgbImage[y * IMAGE_WIDTH + x] = (0xFF << 24) + (r << 16) + (g << 8) + b;
            }
        }

        BufferedImage bufferedImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < IMAGE_HEIGHT; ++y) {
            for (int x = 0; x < IMAGE_WIDTH; ++x) {
                bufferedImage.setRGB(x, y, rgbImage[y * IMAGE_WIDTH + x]);
            }
        }

        Java2DNativeImageLoader imageLoader = new Java2DNativeImageLoader(IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_CHANNELS);
        INDArray image = imageLoader.asMatrix(bufferedImage, true);

        System.out.println(Arrays.toString(image.shape()));

        return image;
    }
    
    public int getSize() {
        return imagesWithLabel.size();
    }
}
