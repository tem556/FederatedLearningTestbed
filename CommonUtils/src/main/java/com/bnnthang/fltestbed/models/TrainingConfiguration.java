package com.bnnthang.fltestbed.models;

import com.bnnthang.fltestbed.servers.IAggregationStrategy;
import lombok.Data;
import lombok.NonNull;

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
}
