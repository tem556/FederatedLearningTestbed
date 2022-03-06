package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.List;

public class FedAvg implements AggregationStrategy {
    @Override
    public INDArray aggregate(List<INDArray> params) {
        INDArray base = Nd4j.zeros(1, 896);
        INDArray sumUpdates = params.stream().reduce(base, INDArray::add);
        return sumUpdates.div(params.size());
    }
}
