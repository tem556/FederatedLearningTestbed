package com.bnnthang.fltestbed.Server.AggregationStrategies;

import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import com.bnnthang.fltestbed.commonutils.servers.IAggregationStrategy;
import org.deeplearning4j.nn.gradient.DefaultGradient;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NewFedAvg implements IAggregationStrategy {
    @Override
    public MultiLayerNetwork aggregate(MultiLayerNetwork model, List<TrainingReport> reports) throws Exception {
        // update gradients
        List<Gradient> gradients = reports.stream().map(TrainingReport::getGradient).collect(Collectors.toList());
        List<Map<String, INDArray>> gradientTables = gradients.stream().map(Gradient::gradientForVariable).collect(Collectors.toList());
        Set<String> variables = gradientTables.get(0).keySet();
        Map<String, INDArray> avgGradientTable = new HashMap<>();
        for (String key : variables) {
            INDArray sum = gradientTables.get(0).get(key);
            for (int i = 1; i < gradientTables.size(); ++i) {
                sum = sum.add(gradientTables.get(i).get(key));
            }
            INDArray avg = sum.div(reports.size());
            avgGradientTable.put(key, avg);
        }
        Gradient g = new DefaultGradient();
        for (String key : avgGradientTable.keySet()) {
            g.setGradientFor(key, avgGradientTable.get(key));
        }
        model.setGradient(g);

        // update model params
        INDArray sum = reports.get(0).getParams();
        for (int i = 1; i < reports.size(); ++i)
            sum = sum.add(reports.get(i).getParams());
        INDArray avg = sum.div(reports.size());
        model.setParams(avg);

        return model;
    }
}
