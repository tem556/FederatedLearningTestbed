package com.bnnthang.fltestbed.androidclient;

import android.graphics.Bitmap;

import com.bnnthang.fltestbed.commonutils.clients.IClientLocalRepository;
import com.bnnthang.fltestbed.commonutils.models.IDatasetLoader;

import org.datavec.image.loader.AndroidNativeImageLoader;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.util.FeatureUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndroidChestXrayLoader implements IDatasetLoader {
    /**
     * Label size (in bytes).
     */
    protected final static int LABEL_SIZE = 1;

    /**
     * Image height (in pixels).
     */
    protected final static int IMAGE_HEIGHT = 180;

    /**
     * Image width (in pixels).
     */
    protected final static int IMAGE_WIDTH = 180;

    /**
     * Image channels.
     */
    protected final static int IMAGE_CHANNELS = 3;

    /**
     * Image size (in bytes).
     */
    protected final static int IMAGE_SIZE = IMAGE_WIDTH * IMAGE_HEIGHT * IMAGE_CHANNELS;

    /**
     * Row size (in bytes).
     */
    protected final static int ROW_SIZE = LABEL_SIZE + IMAGE_SIZE;

    /**
     * Number of outcomes.
     */
    protected final static int OUTCOMES = 2;

    /**
     * Local file repository.
     */
    protected IClientLocalRepository localRepository;

    /**
     * Instantiate <code>AndroidCifar10Loader</code>
     *
     * @param _localRepository an instance of <code>ILocalRepository</code>
     */
    public AndroidChestXrayLoader(IClientLocalRepository _localRepository) {
        localRepository = _localRepository;
    }

    /**
     * Count the number of elements in the dataset
     * @return the number of elements in the dataset
     */
    @Override
    public long count() throws IOException {
        return localRepository.getDatasetSize() / ROW_SIZE;
    }

    /**
     * Count the number of occurrences of each label
     * @return a map showing the data distribution for all labels
     * @throws IOException if I/O errors happen
     */
    @Override
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

        return frequency;
    }

    /**
     * Create dataset.
     * @param batchSize batch size
     * @param fromIndex starting index
     * @return a dataset that contains images from index <code>fromIndex</code>
     * to <code>fromIndex + batchSize - 1</code> inclusive
     * @throws IOException if I/O errors occur
     */
    @Override
    public DataSet createDataSet(int batchSize, int fromIndex) throws IOException {
        if (!localRepository.datasetExists()) {
            return DataSet.empty();
        }

        InputStream inputStream = localRepository.getDatasetInputStream();
        inputStream.skip((long) fromIndex * ROW_SIZE);

        List<DataSet> atomicDataSets = new ArrayList<>();
        int toIndex = (int) Math.min(count(), fromIndex + batchSize);
        for (int i = fromIndex; i < toIndex; ++i) {
            Pair<Byte, byte[]> row = readOneRow(inputStream);
            INDArray image = bytesToImage(row.getSecond());
            INDArray label = FeatureUtil.toOutcomeVector(row.getFirst(), OUTCOMES);
            atomicDataSets.add(new DataSet(image, label));
        }

        return DataSet.merge(atomicDataSets);
    }

    /**
     * Convert image byte array to <code>INDArray</code>.
     * @param imageBytes byte array of an image
     * @return a corresponding <code>INDArray</code> instance
     * @throws IOException if I/O errors happen
     */
    @Override
    public INDArray bytesToImage(byte[] imageBytes) throws IOException {
        int[] rgbaImage = new int[IMAGE_HEIGHT * IMAGE_WIDTH];
        for (int y = 0; y < IMAGE_HEIGHT; ++y) {
            for (int x = 0; x < IMAGE_WIDTH; ++x) {
                int r = imageBytes[y * IMAGE_WIDTH + x];
                int g = imageBytes[IMAGE_HEIGHT * IMAGE_WIDTH + y * IMAGE_WIDTH + x];
                int b = imageBytes[2 * IMAGE_HEIGHT * IMAGE_WIDTH + y * IMAGE_WIDTH + x];
                rgbaImage[y * IMAGE_WIDTH + x] = (255 << 24) + (r << 16) + (g << 8) + b;
            }
        }

        Bitmap bmp = Bitmap.createBitmap(rgbaImage, IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
        AndroidNativeImageLoader imageLoader = new AndroidNativeImageLoader(IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_CHANNELS);
        INDArray arr = imageLoader.asMatrix(bmp);

        for (int y = 0; y < IMAGE_HEIGHT; ++y) {
            for (int x = 0; x < IMAGE_WIDTH; ++x) {
                double b = arr.getDouble(0, 0, y, x);
                double r = arr.getDouble(0, 2, y, x);
                arr.putScalar(0, 0, y, x, r);
                arr.putScalar(0, 2, y, x, b);
            }
        }

        return arr;
    }

    /**
     * Read one row in the dataset.
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
