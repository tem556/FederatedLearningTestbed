package com.bnnthang.fltestbed.Server.Repositories;

import com.bnnthang.fltestbed.Server.ServerCifar10DataSetIterator;
import com.bnnthang.fltestbed.Server.ServerCifar10Loader;
import com.bnnthang.fltestbed.commonutils.servers.IServerLocalRepository;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Cifar10Repository implements IServerLocalRepository {
    private static final Logger _logger = LogManager.getLogger(Cifar10Repository.class);

    private final String workingDirectory;
    private String currentModelName = "newmodel.zip";
    private String currentResultFileName = null;

    private final Map<Byte, List<byte[]>> imagesByLabel;

    public Cifar10Repository(String _workingDirectory) throws IOException {
        workingDirectory = _workingDirectory;

        imagesByLabel = new HashMap<>();

        // load dataset
        load(new FileInputStream(workingDirectory + "/cifar-10/data_batch_1.bin"));
        load(new FileInputStream(workingDirectory + "/cifar-10/data_batch_2.bin"));
        load(new FileInputStream(workingDirectory + "/cifar-10/data_batch_3.bin"));
        load(new FileInputStream(workingDirectory + "/cifar-10/data_batch_4.bin"));
        load(new FileInputStream(workingDirectory + "/cifar-10/data_batch_5.bin"));
    }

    private void load(InputStream inputStream) throws IOException {
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

            imagesByLabel.putIfAbsent(labelBytes[0], new ArrayList<>());
            imagesByLabel.get(labelBytes[0]).add(imageBytes);
        }
    }

    public List<List<byte[]>> partialSplitDatasetIIDAndShuffle(int nPartitions, float ratio) {
        List<List<Pair<byte[], Byte>>> partitions = new ArrayList<>();
        for (int i = 0; i < nPartitions; ++i) {
            partitions.add(new ArrayList<>());
        }
        int partition = 0;
        for (byte label = 0; label < 10; ++label) {
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
                byte[] t = new byte[32 * 32 * 3 + 1];
                t[0] = partitions.get(i).get(j).getRight();
                System.arraycopy(partitions.get(i).get(j).getLeft(), 0, t, 1, 32 * 32 * 3);
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
//            _logger.debug(String.format("??? %d %d %d %d", bytes[0], bytes[1], bytes[2], bytes[3]));
            System.arraycopy(bytes, 0, res, cnt, bytes.length);
            cnt += bytes.length;
        }
//        if (cnt != (32 * 32 * 3 + 1) * dataset.size()) {
//            _logger.error("wrong dataset!");
//        }
        return res;
    }

    @Override
    public List<byte[]> partitionAndSerializeDataset(int numPartitions, float ratio) {
        List<List<byte[]>> partitions = partialSplitDatasetIIDAndShuffle(numPartitions, ratio);
        return partitions.stream().map(Cifar10Repository::flatten).collect(Collectors.toList());
    }

    @Override
    public MultiLayerNetwork loadLatestModel() throws IOException {
        _logger.debug("loading " + workingDirectory + "/" + currentModelName);

        return ModelSerializer.restoreMultiLayerNetwork(workingDirectory + "/" + currentModelName);
    }

    @Override
    public byte[] loadAndSerializeLatestModel() throws IOException {
        File f = new File(workingDirectory, currentModelName);
        int modelLength = (int)f.length();
        FileInputStream fis = new FileInputStream(f);
        byte[] bytes = new byte[modelLength];
        int readBytes = fis.read(bytes, 0, modelLength);
        if (readBytes != modelLength) {
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
        String newModelName = "model-" + (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime())) + ".zip";
        newModel.save(new File(workingDirectory, newModelName));
        currentModelName = newModelName;
    }

    @Override
    public void createNewResultFile() throws IOException {
//        String newResultFileName = "result-" + (new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime())) + ".csv";
//        currentResultFileName = newResultFileName;
//        File newResultFile = new File(workingDirectory, newResultFileName);
//        newResultFile.createNewFile();
//        FileWriter writer = new FileWriter(newResultFile, true);
//        writer.write("accuracy,precision,recall,f1,training time (ms),downlink time (ms),uplink time (ms),communicating power (j), computing power (j)\n");
//        writer.close();
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
            File testDatasetFile = new File(workingDirectory, "cifar-10/test_batch.bin");
            ServerCifar10Loader loader = new ServerCifar10Loader(testDatasetFile, 123456);
            ServerCifar10DataSetIterator cifarEval = new ServerCifar10DataSetIterator(loader, 123, 1, 123456);
            MultiLayerNetwork model = loadLatestModel();
            Evaluation eval = model.evaluate(cifarEval);
            model.close();
            return eval;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
