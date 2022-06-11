package com.bnnthang.fltestbed.commonutils.models;

import lombok.Data;

@Data
public class WantedMetrics {
    /**
     * Training time (in milliseconds).
     */
    private Double trainingTime;

    /**
     * Total resources that clients uploaded to server (bytes).
     */
    private Long uplinkBytes;

    /**
     * Total time for clients to upload resources to server (milliseconds per byte).
     */
    private Double uplinkTime;

    /**
     * Total resources that clients downloaded from server (bytes).
     */
    private Long downlinkBytes;

    /**
     * Total time for clients to download resources from server (milliseconds per byte).
     */
    private Double downlinkTime;

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
