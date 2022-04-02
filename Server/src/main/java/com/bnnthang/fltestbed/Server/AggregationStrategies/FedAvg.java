package com.bnnthang.fltestbed.Server.AggregationStrategies;

import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import com.bnnthang.fltestbed.commonutils.servers.IAggregationStrategy;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.List;

public class FedAvg implements IAggregationStrategy {
    @Override
    public MultiLayerNetwork aggregate(MultiLayerNetwork model, List<TrainingReport> reports) throws Exception {
        // update model params
        INDArray sum = reports.get(0).getParams();
        for (int i = 1; i < reports.size(); ++i)
            sum = sum.add(reports.get(i).getParams());
        INDArray avg = sum.div(reports.size());
        model.setParams(avg);

        return model;
    }
}
