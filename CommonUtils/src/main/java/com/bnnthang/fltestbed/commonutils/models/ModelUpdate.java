package com.bnnthang.fltestbed.commonutils.models;

import lombok.Data;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.Serializable;

@Data
public class ModelUpdate implements Serializable {
    /**
     * Weight update.
     */
    private INDArray weight = null;

    public void dispose() {
        if (weight != null) {
            weight.close();
            weight = null;
        }
    }

    public ModelUpdate() {
        weight = null;
    }
}
