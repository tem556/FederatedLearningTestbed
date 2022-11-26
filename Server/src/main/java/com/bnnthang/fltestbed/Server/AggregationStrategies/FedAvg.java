package com.bnnthang.fltestbed.Server.AggregationStrategies;

import com.bnnthang.fltestbed.commonutils.models.ModelUpdate;
import com.bnnthang.fltestbed.commonutils.servers.IAggregationStrategy;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.ArrayList;
import java.util.List;

public class FedAvg implements IAggregationStrategy {
    private final ArrayList<Double> weights;
    public FedAvg(ArrayList<Double> _weights) {
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
