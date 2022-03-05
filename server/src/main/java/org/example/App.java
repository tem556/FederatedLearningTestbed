package org.example;

import com.google.common.primitives.Ints;
import org.datavec.image.loader.CifarLoader;
import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.deeplearning4j.datasets.iterator.impl.Cifar10DataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println( "Hello World!" );
//        FederatedLearningServer server = new FederatedLearningServerImpl();
//        server.startServer();
//        int t = 4715066;
//        byte[] b = Ints.toByteArray(t);
//        System.out.println(b.length);

        int height = 32;
        int width = 32;
        int channels = 3;
        int numLabels = CifarLoader.NUM_LABELS;
        int batchSize = 96;
        long seed = 123L;
        int epochs = 4;

        Cifar10DataSetIterator cifar = new Cifar10DataSetIterator(batchSize, new int[]{height, width}, DataSetType.TRAIN, null, seed);
        Cifar10DataSetIterator cifarEval = new Cifar10DataSetIterator(batchSize, new int[]{height, width}, DataSetType.TEST, null, seed);

        List<String> rawLabels = new Cifar10DataSetIterator(1).getLabels();
        for (String i : rawLabels) {
            System.out.println(i);
        }

        System.out.println("ok");
    }
}
