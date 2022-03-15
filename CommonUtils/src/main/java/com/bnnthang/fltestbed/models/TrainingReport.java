package com.bnnthang.fltestbed.models;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.Serializable;
import java.util.List;

/**
 * Report model for each training.
 */
@Data
public class TrainingReport implements Serializable {
    /**
     * Weight updates for all layers in neural network.
     */
    @NonNull
    private List<INDArray> layerParams;

    /**
     * Training time (in seconds).
     */
    @NonNull
    private Long trainingTimeInSecs;
}
