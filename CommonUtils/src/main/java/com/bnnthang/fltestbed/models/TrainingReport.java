package com.bnnthang.fltestbed.models;

import lombok.Getter;
import lombok.Setter;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.Serializable;
import java.util.List;

/**
 * Report model for each training
 */
public class TrainingReport implements Serializable {
    /**
     * Weight updates for all layers in neural network.
     */
    @Getter
    @Setter
    private List<INDArray> layerParams;

    /**
     * Training time (in seconds).
     */
    @Getter
    @Setter
    private Long trainingTimeInSecs;
}
