package com.bnnthang.fltestbed.Server.Repositories;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Cifar10Repository {
    private Map<Byte, List<byte[]>> imagesByLabel;

    public Cifar10Repository() throws IOException {
        imagesByLabel.clear();
        load("cifar-10/data_batch_1.bin");
        load("cifar-10/data_batch_2.bin");
        load("cifar-10/data_batch_3.bin");
        load("cifar-10/data_batch_4.bin");
        load("cifar-10/data_batch_5.bin");
    }

    private void load(String resourcePath) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(resourcePath);
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

    public List<List<Pair<byte[], Byte>>> splitDatasetIID(int partitions) {
        List<List<Pair<byte[], Byte>>> res = new ArrayList<>();
        for (int i = 0; i < partitions; ++i) {
            res.add(new ArrayList<>());
        }
        int partition = 0;
        for (byte label = 0; label < 10; ++label) {
            for (byte[] image : imagesByLabel.get(label)) {
                res.get(partition).add(Pair.of(image, label));

                ++partition;
                partition %= 10;
            }
        }
        return res;
    }
}
