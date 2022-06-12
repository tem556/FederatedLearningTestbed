package com.bnnthang.fltestbed.commonutils.models;

import lombok.Data;

import java.io.Serializable;

@Data
public class WantedMetrics implements Serializable {
    /**
     * Training time (in milliseconds).
     */
    private double trainingTime = 0.0;

    /**
     * Total resources that clients uploaded to server (bytes).
     */
    private long uplinkBytes = 0;

    /**
     * Total time for clients to upload resources to server (milliseconds per byte).
     */
    private double uplinkTime = 0.0;

    /**
     * Total resources that clients downloaded from server (bytes).
     */
    private long downlinkBytes = 0;

    /**
     * Total time for clients to download resources from server (milliseconds per byte).
     */
    private double downlinkTime = 0.0;

    public String[] toCsvLine() {
        return new String[] {
                String.valueOf(trainingTime),
                String.valueOf(uplinkBytes),
                String.valueOf(uplinkTime),
                String.valueOf(downlinkBytes),
                String.valueOf(downlinkTime)
        };
    }
}
