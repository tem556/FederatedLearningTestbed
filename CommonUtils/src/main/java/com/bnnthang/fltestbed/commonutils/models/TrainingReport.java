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
    private INDArray params;

//    private INDArray stateViewArray;

    /**
     * Training time (in milliseconds).
     */
    private Double trainingTime;

    /**
     * Total time for clients to download resources from server (milliseconds per byte).
     */
    private Double downlinkTime;

    /**
     * Communicating power consumption (joules).
     */
    private PowerConsumptionFromBytes communicationPower;

    /**
     * Computing power consumption (joules)
     */
    private Double computingPower;

    public TrainingReport() {
        params = null;
        trainingTime = null;
        downlinkTime = null;
        communicationPower = new PowerConsumptionFromBytes();
        computingPower = null;
    }
}
