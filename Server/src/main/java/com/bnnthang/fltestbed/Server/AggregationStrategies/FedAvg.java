package com.bnnthang.fltestbed.Server.AggregationStrategies;

import com.bnnthang.fltestbed.commonutils.models.ModelUpdate;
import com.bnnthang.fltestbed.commonutils.servers.IAggregationStrategy;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.List;

public class FedAvg implements IAggregationStrategy {
    @Override
    public MultiLayerNetwork aggregate(MultiLayerNetwork model, List<ModelUpdate> updates) throws Exception {
        // update model params
        INDArray sum = updates.get(0).getWeight();
        for (int i = 1; i < updates.size(); ++i)
            sum = sum.add(updates.get(i).getWeight());
        INDArray avg = sum.div(updates.size());
        model.setParams(avg);

        avg.close();
        sum.close();

        return model;
    }
}
