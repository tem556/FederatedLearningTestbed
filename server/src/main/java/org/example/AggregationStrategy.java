package org.example;

import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.List;

public interface AggregationStrategy {
    INDArray aggregate(List<INDArray> params);
}
