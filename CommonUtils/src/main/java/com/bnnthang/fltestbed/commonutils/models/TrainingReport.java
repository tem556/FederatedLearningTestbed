package com.bnnthang.fltestbed.commonutils.models;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.deeplearning4j.nn.gradient.Gradient;
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
//    private List<INDArray> layerParams;
    private Gradient gradient;

    @NonNull
    private INDArray params;

    /**
     * Training time (in seconds).
     */
    @NonNull
    private Long trainingTimeInSecs;
}
