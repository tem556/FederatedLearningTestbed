package com.bnnthang.fltestbed.commonutils.models;

import lombok.Data;
import org.nd4j.linalg.api.ndarray.INDArray;

@Data
public class ModelUpdate {
    private INDArray weight;

    public ModelUpdate() {
        weight = null;
    }
}
