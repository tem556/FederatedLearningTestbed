package com.bnnthang.fltestbed.commonutils.clients;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.util.FeatureUtil;
import org.opencv.core.Mat;
import org.opencv.core.CvType;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MyCifar10Loader {
    /**
     * Label size (in bytes).
     */
    private final static int LABEL_SIZE = 1;

    /**
     * Image height (in pixels).
     */
    private final static int IMAGE_HEIGHT = 32;

    /**
     * Image width (in pixels).
     */
    private final static int IMAGE_WIDTH = 32;

    /**
     * Image channels.
     */
    private final static int IMAGE_CHANNELS = 3;

    /**
     * Image size (in bytes).
     */
    private final static int IMAGE_SIZE = IMAGE_WIDTH * IMAGE_HEIGHT * IMAGE_CHANNELS;

    /**
     * Row size (in bytes).
     */
    private final static int ROW_SIZE = LABEL_SIZE + IMAGE_SIZE;

    /**
     * Local file repository.
     */
    private IClientLocalRepository localRepository;

    /**
     * Logger.
     */
    private static final Logger _logger = LogManager.getLogger(MyCifar10Loader.class);

    /**
     * Instantiate <code>MyCifar10Loader</code>
     * @param _localRepository an instance of <code>ILocalRepository</code>
     */
    public MyCifar10Loader(IClientLocalRepository _localRepository) {
        localRepository = _localRepository;
    }

    /**
     * Count the number of elements in the dataset
     * @return the number of elements in the dataset
     */
    public long count() throws IOException {
        return localRepository.getDatasetSize() / ROW_SIZE;
    }

    /**
     * Count the number of occurrences of each label
     * @return a map showing the data distribution for all labels
     * @throws IOException if I/O errors happen
     */
    public Map<Integer, Integer> getDataDistribution() throws IOException {
        Map<Integer, Integer> frequency = new HashMap<>();

        // open dataset file stream
        InputStream inputStream = localRepository.getDatasetInputStream();

        // read until end of file
        int cnt = 0;
        while (inputStream.available() >= ROW_SIZE) {
//            _logger.debug("image id = " + cnt);
            ++cnt;
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
     * @throws IOException
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
            INDArray label = FeatureUtil.toOutcomeVector(row.getFirst(), 10);
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
    private INDArray bytesToImage(byte[] imageBytes) throws IOException {
        // initialize an image matrix of 32x32 that has 3 color channels,
        // uses 8 bits each
        Mat mat = new Mat(IMAGE_HEIGHT, IMAGE_WIDTH, CvType.CV_8UC3);
        mat.put(0, 0, imageBytes);

        NativeImageLoader nativeImageLoader = new NativeImageLoader();
        INDArray image = nativeImageLoader.asMatrix(mat);

        return image.reshape(1, IMAGE_CHANNELS, IMAGE_HEIGHT, IMAGE_WIDTH);
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

//        if (_logger.isDebugEnabled()) {
//            _logger.debug("label = " + labelBytes[0]);
//            _logger.debug("img = " + Arrays.asList(imageBytes[0], imageBytes[1], imageBytes[2]));
//        }

        if (bytesRead != ROW_SIZE) {
            throw new IOException(String.format("didn't read enough %d bytes", ROW_SIZE));
        }
        return new Pair<>(labelBytes[0], imageBytes);
    }
}
