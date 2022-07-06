package com.bnnthang.fltestbed.commonutils.models;

import com.bnnthang.fltestbed.commonutils.servers.IAggregationStrategy;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.json.simple.*;

/**
 * Encapsulate training configurations.
 */
@Data
public class TrainingConfiguration {
    /**
     * The minimum number of clients in a training round.
     */
    @NonNull
    private Integer minClients;

    /**
     * Number of rounds in a training iteration.
     */
    @NonNull
    private Integer rounds;

    /**
     * Poll interval (in milliseconds).
     */
    @NonNull
    private Integer pollInterval;

    /**
     * The aggregation strategy to use.
     */
    @NonNull
    private IAggregationStrategy aggregationStrategy;

    /**
     * Percentage of the dataset to train on.
     */
    @NonNull
    private Float datasetRatio;

    /**
     * True if JSONObject should be used
     */
    @NonNull
    private Boolean useConfig;

    /**
     * JSON Object that contains quantity and timing of the dropped clients
     * Can be null
     */
    private JSONObject jsonObject;

}
