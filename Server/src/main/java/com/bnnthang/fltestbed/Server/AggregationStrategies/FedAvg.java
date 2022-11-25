package com.bnnthang.fltestbed.Server.AggregationStrategies;

import  com.bnnthang.fltestbed.commonutils.models.ModelUpdate;
import com.bnnthang.fltestbed.commonutils.servers.IAggregationStrategy;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.List;
import java.io.IOException;

public class FedAvg implements IAggregationStrategy {
    private final List<Double> weights;
    public FedAvg(List<Double> _weights) {
        weights = _weights;
    }
    @Override
    public MultiLayerNetwork aggregate(MultiLayerNetwork model, List<ModelUpdate> updates) throws Exception {
        // update model params
        INDArray avg = updates.get(0).getWeight().mul(weights.get(0));
        for (int i = 1; i < updates.size(); ++i)
            avg = avg.add(updates.get(i).getWeight().mul(weights.get(i)));
        model.setParams(avg);

        avg.close();

        return model;
    }
}
