package com.bnnthang.fltestbed.Server.Repositories;

import com.bnnthang.fltestbed.Server.App;
import com.bnnthang.fltestbed.Server.ServerCifar10DataSetIterator;
import com.bnnthang.fltestbed.Server.ServerCifar10Loader;
import com.bnnthang.fltestbed.commonutils.clients.MyCifar10Loader;
import com.bnnthang.fltestbed.commonutils.servers.IServerLocalRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.classification.Evaluation;

import java.io.*;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Cifar10Repository implements IServerLocalRepository {
    private final String modelDirectory;
    private String currentModelName = "newmodel.zip";
    private String currentResultFileName = null;

    private final Map<Byte, List<byte[]>> imagesByLabel;

    public Cifar10Repository(String modelDir) throws IOException, URISyntaxException {
        modelDirectory = modelDir;

        imagesByLabel = new HashMap<>();

//        System.out.println("length = " + new File(getClass().getResource("cifar-10/test_batch_1.bin").toExternalForm()));

        load(Objects.requireNonNull(App.class.getClassLoader().getResourceAsStream("cifar-10/data_batch_1.bin")));
        load(Objects.requireNonNull(App.class.getClassLoader().getResourceAsStream("cifar-10/data_batch_2.bin")));
        load(Objects.requireNonNull(App.class.getClassLoader().getResourceAsStream("cifar-10/data_batch_3.bin")));
        load(Objects.requireNonNull(App.class.getClassLoader().getResourceAsStream("cifar-10/data_batch_4.bin")));
        load(Objects.requireNonNull(App.class.getClassLoader().getResourceAsStream("cifar-10/data_batch_5.bin")));
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

    public List<List<byte[]>> splitDatasetIIDAndShuffle(int nPartitions) {
        List<List<Pair<byte[], Byte>>> partitions = new ArrayList<>();
        for (int i = 0; i < nPartitions; ++i) {
            partitions.add(new ArrayList<>());
        }
        int partition = 0;
        for (byte label = 0; label < 10; ++label) {
            for (byte[] image : imagesByLabel.get(label)) {
                partitions.get(partition).add(Pair.of(image, label));

                ++partition;
                partition %= nPartitions;
            }
        }
        List<List<byte[]>> res = new ArrayList<>();
        for (int i = 0; i < nPartitions; ++i) {
            res.add(new ArrayList<>());
            for (int j = 0; j < partitions.get(i).size(); ++j) {
                byte[] t = new byte[partitions.get(i).get(j).getLeft().length + 1];
                t[0] = partitions.get(i).get(j).getRight();
                System.arraycopy(partitions.get(i).get(j).getLeft(), 0, t, 1, partitions.get(i).get(j).getLeft().length);
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
        for (int i = 0; i < dataset.size(); ++i) {
            length += dataset.get(i).length;
        }
        return length;
    }

    private static byte[] flatten(List<byte[]> dataset) {
        int length = DatasetLength(dataset);
        byte[] res = new byte[length];
        int cnt = 0;
        for (int i = 0; i < dataset.size(); ++i) {
            System.arraycopy(dataset.get(i), 0, res, cnt, dataset.get(i).length);
            cnt += dataset.get(i).length;
        }
        return res;
    }

    @Override
    public List<byte[]> partitionAndSerializeDataset(int numPartitions) {
        List<List<byte[]>> partitions = splitDatasetIIDAndShuffle(numPartitions);
        return partitions.stream().map(Cifar10Repository::flatten).collect(Collectors.toList());
    }

    @Override
    public MultiLayerNetwork loadLatestModel() throws IOException {
        return ModelSerializer.restoreMultiLayerNetwork(modelDirectory + "/" + currentModelName);
    }

    @Override
    public byte[] loadAndSerializeLatestModel() throws IOException {
        File f = new File(modelDirectory, currentModelName);
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
    public void saveNewModel(MultiLayerNetwork newModel) throws IOException {
        String newModelName = "model" + (new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(Calendar.getInstance().getTime())) + ".zip";
        newModel.save(new File(modelDirectory, newModelName));
        currentModelName = newModelName;
    }

    @Override
    public void createNewResultFile() throws IOException {
        String newResultFileName = "result-" + (new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(Calendar.getInstance().getTime())) + ".csv";
        currentResultFileName = newResultFileName;
        File newResultFile = new File(modelDirectory, newResultFileName);
        if (newResultFile.createNewFile()) {
            FileWriter writer = new FileWriter(newResultFile, true);
            writer.write("accuracy,precision,recall,f1,training time (s),downlink time (s),uplink time (s)\n");
            writer.close();
        } else {
            throw new IOException("cannot create file");
        }
    }

    @Override
    public void appendToCurrentFile(String s) throws IOException {
        File currentResultFile = new File(modelDirectory, currentResultFileName);
        FileWriter writer = new FileWriter(currentResultFile, true);
        writer.write(s);
        writer.close();
    }

    @Override
    public Evaluation evaluateCurrentModel() {
        try {
            File testDatasetFile = new File(App.class.getClassLoader().getResource("cifar-10/test_batch.bin").toURI());
            ServerCifar10Loader loader = new ServerCifar10Loader(testDatasetFile, 123456);
            ServerCifar10DataSetIterator cifarEval = new ServerCifar10DataSetIterator(loader, 123, 1, 123456);
            MultiLayerNetwork model = loadLatestModel();
            return model.evaluate(cifarEval);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
