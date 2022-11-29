package com.bnnthang.fltestbed.commonutils.models;

import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import org.datavec.image.loader.Java2DNativeImageLoader;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.util.FeatureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class ChestXrayDatasetLoader implements IDatasetLoader {
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

    /**
     * Local file repository.
     */
    protected IClientLocalRepository localRepository;

    /**
     * Logger.
     */
    private static final Logger _logger = LoggerFactory.getLogger(ChestXrayDatasetLoader.class);

    private long rowCount = -1;

    /**
     * Instantiate <code>ChestXrayLoader</code>
     * 
     * @param _localRepository an instance of <code>ILocalRepository</code>
     */
    public ChestXrayDatasetLoader(IClientLocalRepository _localRepository) {
        localRepository = _localRepository;
    }

    /**
     * Count the number of elements in the dataset
     * 
     * @return the number of elements in the dataset
     */
    public long count() throws IOException {
        if (rowCount < 0) {
            rowCount = localRepository.getDatasetSize() / ROW_SIZE;
        }
        return rowCount;
    }

    /**
     * Count the number of occurrences of each label
     * 
     * @return a map showing the data distribution for all labels
     * @throws IOException if I/O errors happen
     */
    public Map<Integer, Integer> getDataDistribution() throws IOException {
        Map<Integer, Integer> frequency = new HashMap<>();

        // open dataset file stream
        InputStream inputStream = localRepository.getDatasetInputStream();

        // read until end of file
        while (inputStream.available() >= ROW_SIZE) {
            Pair<Byte, byte[]> row = readOneRow(inputStream);
            Integer currentFrequency = frequency.getOrDefault((int) row.getFirst(), 0);
            frequency.put((int) row.getFirst(), currentFrequency + 1);
        }

        inputStream.close();

        return frequency;
    }

    /**
     * Create dataset.
     * 
     * @param batchSize batch size
     * @param fromIndex starting index
     * @return a dataset that contains images from index <code>fromIndex</code>
     *         to <code>fromIndex + batchSize - 1</code> inclusive
     * @throws IOException if I/O errors occur
     */
    public DataSet createDataSet(int batchSize, int fromIndex) throws IOException {
        if (!localRepository.datasetExists())
            return DataSet.empty();

        InputStream inputStream = localRepository.getDatasetInputStream();
        inputStream.skip((long) fromIndex * ROW_SIZE);

        List<DataSet> atomicDataSets = new ArrayList<>();
        int toIndex = (int) Math.min(count(), fromIndex + batchSize);
        for (int i = fromIndex; i < toIndex; ++i) {
            Pair<Byte, byte[]> row = readOneRow(inputStream);
            INDArray image = bytesToImage(row.getSecond());
            INDArray label = FeatureUtil.toOutcomeVector(row.getFirst(), 2);
            atomicDataSets.add(new DataSet(image, label));
        }

        inputStream.close();

        return DataSet.merge(atomicDataSets);
    }

    /**
     * Convert image byte array to <code>INDArray</code>.
     * 
     * @param imageBytes byte array of an image
     * @return a corresponding <code>INDArray</code> instance
     * @throws IOException if I/O errors happen
     */
    public INDArray bytesToImage(byte[] imageBytes) throws IOException {
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

        return imageLoader.asMatrix(bufferedImage, true);
    }

    /**
     * Read one row in the dataset.
     * 
     * @param inputStream the input stream to the dataset
     * @return a pair contains a label and an image
     * @throws IOException if I/O errors happen
     */
    private Pair<Byte, byte[]> readOneRow(InputStream inputStream) throws IOException {
        byte[] labelBytes = new byte[LABEL_SIZE];
        byte[] imageBytes = new byte[IMAGE_SIZE];
        int bytesRead = inputStream.read(labelBytes) + inputStream.read(imageBytes);

        if (bytesRead != ROW_SIZE) {
            throw new IOException(String.format("didn't read enough %d bytes", ROW_SIZE));
        }

        return new Pair<>(labelBytes[0], imageBytes);
    }
}
