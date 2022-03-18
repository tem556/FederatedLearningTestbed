package com.bnnthang.fltestbed.Server.AggregationStrategies;

import com.bnnthang.fltestbed.Server.App;
import com.bnnthang.fltestbed.Server.ServerOperations;
import com.bnnthang.fltestbed.dataset.MyCifar10DataSetIterator;
import com.bnnthang.fltestbed.dataset.MyCifar10Loader;
import com.bnnthang.fltestbed.models.TrainingReport;
import com.bnnthang.fltestbed.servers.IAggregationStrategy;
import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.deeplearning4j.datasets.iterator.impl.Cifar10DataSetIterator;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.gradient.DefaultGradient;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.IEvaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NewFedAvg implements IAggregationStrategy {
    @Override
    public MultiLayerNetwork aggregate(MultiLayerNetwork model, List<TrainingReport> reports) throws Exception {
//        List<Gradient> gradients = reports.stream().map(TrainingReport::getGradient).collect(Collectors.toList());
//        List<Map<String, INDArray>> gradientTables = gradients.stream().map(Gradient::gradientForVariable).collect(Collectors.toList());
//        Set<String> variables = gradientTables.get(0).keySet();
//        Map<String, INDArray> avgGradientTable = new HashMap<>();
//        for (String key : variables) {
//            INDArray sum = gradientTables.get(0).get(key);
//            for (int i = 1; i < gradientTables.size(); ++i) {
//                sum = sum.add(gradientTables.get(i).get(key));
//            }
//            INDArray avg = sum.div(reports.size());
//            avgGradientTable.put(key, avg);
//        }
//        Gradient g = new DefaultGradient();
//        for (String key : avgGradientTable.keySet()) {
//            g.setGradientFor(key, avgGradientTable.get(key));
//        }
//        model.setGradient(g);

        INDArray sum = reports.get(0).getParams();
        for (int i = 1; i < reports.size(); ++i)
            sum = sum.add(reports.get(i).getParams());
        INDArray avg = sum.div(reports.size());
        model.setParams(avg);

//        int layers = reports.get(0).getLayerParams().size();
//        for (int i = 0; i < layers; ++i) {
//            if (reports.get(0).getLayerParams().get(i) == null) {
//                continue;
//            }
//
//            INDArray identity = Nd4j.zeros(reports.get(0).getLayerParams().get(i).shape());
//            int finalI = i;
//            INDArray sumUpdates = reports.stream().map(x -> x.getLayerParams().get(finalI)).reduce(identity, INDArray::add);
//            INDArray avgUpdates = sumUpdates.div(reports.size());
//            model.getLayer(i).setParams(avgUpdates);
//        }

        // eval
        String path = App.class.getClassLoader().getResource("cifar-10/test_batch.bin").getPath();
        System.out.println(path);
        MyCifar10Loader loader = new MyCifar10Loader(new File(path), 123456);
        MyCifar10DataSetIterator cifarEval = new MyCifar10DataSetIterator(loader, 123, 1, 123456);
//        Cifar10DataSetIterator cifarEval = new Cifar10DataSetIterator(123, new int[]{32, 32}, DataSetType.TEST, null, 245L);
        IEvaluation evaluation = model.evaluate(cifarEval);
        FileWriter writer = new FileWriter("C:\\Users\\buinn\\DoNotTouch\\crap\\testbed\\test3.txt", true);
        writer.write(evaluation.stats());
        writer.close();
//        System.out.println(evaluation.stats());

        return model;

//        // overwrite new model
//        ModelSerializer.writeModel(model, ServerOperations.MODEL_PATH, true);
    }
}
