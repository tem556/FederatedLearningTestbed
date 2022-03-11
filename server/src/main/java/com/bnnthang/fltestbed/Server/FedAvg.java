package com.bnnthang.fltestbed.Server;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.List;

public class FedAvg implements AggregationStrategy {
    @Override
    public INDArray aggregate(List<INDArray> params) {
        INDArray base = Nd4j.zeros(params.get(0).shape());
        INDArray sumUpdates = params.stream().reduce(base, INDArray::add);
        return sumUpdates.div(params.size());
    }
}
