package com.bnnthang.fltestbed.commonutils.models;

import lombok.Data;

import java.io.Serializable;

/**
 * Report model for each training.
 */
@Data
public class TrainingReport implements Serializable {
    /**
     * Weight updates for all layers in neural network.
     */
    private ModelUpdate modelUpdate;

    private WantedMetrics metrics;

    public TrainingReport() {
        modelUpdate = new ModelUpdate();
        metrics = new WantedMetrics();
    }
}
