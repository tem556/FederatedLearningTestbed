package com.bnnthang.fltestbed.Server.Repositories;

import com.bnnthang.fltestbed.Server.ServerChestXrayDataSetIterator;
import com.bnnthang.fltestbed.Server.ServerChestXrayLoader;
import com.bnnthang.fltestbed.commonutils.servers.IServerLocalRepository;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.simple.*;

public class ChestXrayRepository implements IServerLocalRepository {
    /**
     * Image Width.
     */
    private static final Integer IMAGE_WIDTH = 180;

    /**
     * Image Height.
     */
    private static final Integer IMAGE_HEIGHT = 180;

    /**
     * Image channels
     */
    private static final Integer IMAGE_CHANNELS = 3;
    /**
     * Labels.
     */
    private static final Integer NUM_OF_LABELS = 2;

    /**
     * Logger.
     */
    private static final Logger _logger = LogManager.getLogger(ChestXrayRepository.class);

    private final String workingDirectory;

    private final boolean useConfig;

    private final JSONObject jsonObject;

    // TODO: add some flexibility for this
    private String currentModelName = "xraymodel.zip";

    private final Map<Byte, List<byte[]>> imagesByLabel;

    public ChestXrayRepository(String _workingDirectory, boolean _useConfig, JSONObject _jsonObject)
            throws IOException {
        workingDirectory = _workingDirectory;
        useConfig = _useConfig;
        jsonObject = _jsonObject;

        imagesByLabel = new HashMap<>();

        // load chest x-ray dataset
        load(new FileInputStream(workingDirectory + "/train_batch.bin"));
    }

    private void load(InputStream inputStream) throws IOException {
        int imageSize = IMAGE_WIDTH * IMAGE_HEIGHT * IMAGE_CHANNELS;
        int labelSize = 1;
        int rowSize = imageSize + labelSize;
        while (inputStream.available() >= rowSize) {
            byte[] labelBytes = new byte[labelSize];
            byte[] imageBytes = new byte[imageSize];
            int bytesRead = inputStream.read(labelBytes) + inputStream.read(imageBytes);

            if (bytesRead != rowSize) {
                throw new IOException("read invalid row");
            }

            imagesByLabel.putIfAbsent(labelBytes[0], new ArrayList<>());
            imagesByLabel.get(labelBytes[0]).add(imageBytes);
        }
    }

    public List<List<byte[]>> partialSplitDatasetIIDAndShuffleEvenly(int nPartitions, float ratio) {
        List<List<Pair<byte[], Byte>>> partitions = new ArrayList<>();
        for (int i = 0; i < nPartitions; ++i) {
            partitions.add(new ArrayList<>());
        }
        int partition = 0;
        for (byte label = 0; label < NUM_OF_LABELS; ++label) {
            List<byte[]> images = imagesByLabel.get(label);
            Collections.shuffle(images);
            int nSamples = Integer.min(Math.round(ratio * images.size()), images.size());
            for (int i = 0; i < nSamples; ++i) {
                partitions.get(partition).add(Pair.of(images.get(i), label));
                ++partition;
                partition %= nPartitions;
            }
        }
        List<List<byte[]>> res = new ArrayList<>();
        for (int i = 0; i < nPartitions; ++i) {
            res.add(new ArrayList<>());
            for (int j = 0; j < partitions.get(i).size(); ++j) {
                byte[] t = new byte[IMAGE_HEIGHT * IMAGE_WIDTH * IMAGE_CHANNELS + 1];
                t[0] = partitions.get(i).get(j).getRight();
                System.arraycopy(partitions.get(i).get(j).getLeft(), 0, t, 1, IMAGE_HEIGHT * IMAGE_WIDTH * IMAGE_CHANNELS);
                res.get(i).add(t);
            }
        }

        // shuffle
        for (int i = 0; i < nPartitions; ++i) {
            Collections.shuffle(res.get(i));
        }

        return res;
    }

    // Checks that the ratios for each label in JSONArray distribution sum up to 1
    public boolean isValidLabelDistribution(ArrayList<ArrayList<Double>> distribution) {
        for (int i = 0; i < NUM_OF_LABELS; i++) {
            int finalI = i;
            Double one = 1.0;
            // Make the Stream that contains the ratios for the ith label
            Stream ithRatios = distribution.stream().map(a -> a.get(finalI));
            if (!one.equals(ithRatios.mapToDouble(a -> (double) a).sum())) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidJSON(ArrayList<Double> distributionRatiosC, ArrayList<ArrayList<Double>> distributionRatiosL,
            int nPartitions, boolean even) {
        Double one = 1.0;
        // Only checks array that is being used
        if (even) {
            if (distributionRatiosC.size() != nPartitions)
                return false;
            else
                return (one.equals(distributionRatiosC.stream().mapToDouble(a -> (Double) a).sum()));
        } else {
            if (distributionRatiosL.size() != nPartitions)
                return false;
            else
                return isValidLabelDistribution(distributionRatiosL);
        }
    }

    public List<List<byte[]>> partialSplitDatasetIIDAndShuffle(int nPartitions, float ratio) throws IOException {
        ArrayList<Double> distributionRatiosByClient;
        ArrayList<ArrayList<Double>> distributionRatiosByLabels;
        boolean evenLabelDistribution;
        evenLabelDistribution = (boolean) jsonObject.get("evenLabelDistributionByClient");
        distributionRatiosByClient = (ArrayList<Double>) jsonObject.get("distributionRatiosByClient");
        distributionRatiosByLabels = (ArrayList<ArrayList<Double>>) jsonObject.get("distributionRatiosByLabels");

        if (!isValidJSON(distributionRatiosByClient, distributionRatiosByLabels, nPartitions, evenLabelDistribution)) {
            throw new IOException("Invalid JSON configuration");
        }
        _logger.debug("JSON file passed preconditions");

        List<List<Pair<byte[], Byte>>> partitions = new ArrayList<>();
        for (int i = 0; i < nPartitions; ++i) {
            partitions.add(new ArrayList<>());
        }

        int usedImages;
        int nSamples;
        float subRatio;
        for (byte label = 0; label < NUM_OF_LABELS; ++label) {
            usedImages = 0;
            List<byte[]> images = imagesByLabel.get(label);
            Collections.shuffle(images);
            // Goes through each partition and gives it the number of images it needs
            for (int partition = 0; partition < nPartitions; partition++) {
                if (evenLabelDistribution) {
                    subRatio = distributionRatiosByClient.get(partition).floatValue();
                } else
                    subRatio = (distributionRatiosByLabels.get(partition)).get(label).floatValue();
                nSamples = Integer.min(Math.round(subRatio * ratio * images.size()), images.size());
                for (int i = usedImages; i < usedImages + nSamples; ++i) {
                    assert i < images.size() : "Problem in accessing images";
                    partitions.get(partition).add(Pair.of(images.get(i), label));
                }
                usedImages += nSamples;
            }
        }

        List<List<byte[]>> res = new ArrayList<>();
        for (int i = 0; i < nPartitions; ++i) {
            res.add(new ArrayList<>());
            for (int j = 0; j < partitions.get(i).size(); ++j) {
                byte[] t = new byte[IMAGE_HEIGHT * IMAGE_WIDTH * IMAGE_CHANNELS + 1];
                t[0] = partitions.get(i).get(j).getRight();
                System.arraycopy(partitions.get(i).get(j).getLeft(), 0, t, 1, IMAGE_HEIGHT * IMAGE_WIDTH * IMAGE_CHANNELS);
                res.get(i).add(t);
            }
        }

        // shuffle
        for (int i = 0; i < nPartitions; ++i) {
            Collections.shuffle(res.get(i));
        }

        return res;
    }

    private static int DatasetLength(List<byte[]> dataset) {
        int length = 0;
        for (byte[] bytes : dataset) {
            length += bytes.length;
        }
        return length;
    }

    private static byte[] flatten(List<byte[]> dataset) {
        int length = DatasetLength(dataset);
        byte[] res = new byte[length];
        int cnt = 0;
        for (byte[] bytes : dataset) {
            System.arraycopy(bytes, 0, res, cnt, bytes.length);
            cnt += bytes.length;
        }
        return res;
    }

    @Override
    public List<byte[]> partitionAndSerializeDataset(int numPartitions, float ratio) {
        if (useConfig) {
            try {
                List<List<byte[]>> partitions = partialSplitDatasetIIDAndShuffle(numPartitions, ratio);
                return partitions.stream().map(ChestXrayRepository::flatten).collect(Collectors.toList());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            List<List<byte[]>> partitions = partialSplitDatasetIIDAndShuffleEvenly(numPartitions, ratio);
            return partitions.stream().map(ChestXrayRepository::flatten).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public MultiLayerNetwork loadLatestModel() throws IOException {
        _logger.debug("loading " + workingDirectory + "/" + currentModelName);

        return ModelSerializer.restoreMultiLayerNetwork(workingDirectory + "/" + currentModelName);
    }

    @Override
    public byte[] loadAndSerializeLatestModel() throws IOException {
        File f = new File(workingDirectory, currentModelName);
        int modelLength = (int) f.length();
        FileInputStream fis = new FileInputStream(f);
        byte[] bytes = new byte[modelLength];
        int readBytes = fis.read(bytes, 0, modelLength);
        if (readBytes != modelLength) {
            fis.close();
            throw new IOException(String.format("read %d bytes; expected %d bytes", readBytes, modelLength));
        }
        fis.close();
        return bytes;
    }

    @Override
    public byte[] loadAndSerializeLatestModelWeights() throws IOException {
        MultiLayerNetwork model = loadLatestModel();
        INDArray params = model.params().dup();
        model.close();
        byte[] bytes = SerializationUtils.serialize(params);
        return bytes;
    }

    @Override
    public void saveNewModel(MultiLayerNetwork newModel) throws IOException {
        String newModelName = "xraymodel-"
                + (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime())) + ".zip";
        newModel.save(new File(workingDirectory, newModelName));
        currentModelName = newModelName;
    }

    @Override
    public void createNewResultFile() throws IOException {
        throw new UnsupportedOperationException("Deprecated method.");
    }

    @Override
    public File getLogFolder() throws IOException {
        File logFolder = new File(workingDirectory, "logs");
        logFolder.mkdir();
        return logFolder;
    }

    @Override
    public Evaluation evaluateCurrentModel() {
        try {
            File testDatasetFile = new File(workingDirectory, "test_batch.bin");
            _logger.debug(workingDirectory);
            DataSetIterator iterEval = new ServerChestXrayDataSetIterator(
                    new ServerChestXrayLoader(new File[] { testDatasetFile }, 1.0), 16);
            MultiLayerNetwork model = loadLatestModel();
            Evaluation eval = model.evaluate(iterEval);
            model.close();
            return eval;
        } catch (Exception e) {
            _logger.error(e.getStackTrace());
        }
        return null;
    }
}
