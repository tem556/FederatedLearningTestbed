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
     * Weight updates for all layers in neural utils.
     */
    private Gradient gradient;

    private INDArray params;

    /**
     * Training time (in seconds).
     */
    private Long trainingTimeInSecs;

    /**
     * Total time for clients to download resources from server (seconds per byte)
     */
    private Double downlinkTimeInSecs;

    public TrainingReport() {
        gradient = null;
        params = null;
        trainingTimeInSecs = null;
        downlinkTimeInSecs = null;
    }
}
