package com.bnnthang.fltestbed.commonutils.models;

import lombok.Data;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.Serializable;

@Data
public class ModelUpdate implements Serializable {
    private INDArray weight;

    public ModelUpdate() {
        weight = null;
    }
}
