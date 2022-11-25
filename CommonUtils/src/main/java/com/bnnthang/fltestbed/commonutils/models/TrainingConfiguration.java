package com.bnnthang.fltestbed.commonutils.models;

import com.bnnthang.fltestbed.commonutils.servers.IAggregationStrategy;
import lombok.Data;
import lombok.NonNull;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

/**
 * Encapsulate training configurations.
 */
@Data
@AllArgsConstructor
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
     * Boolean that is only used when useConfig is true. Is true if data is to be distributed among clients
     * with different ratios, but labels remain evenly distributed. Is false if labels are to be distributed unevenly
     */
    private Boolean evenLabelDistribution;

    /**
     * List of doubles where index i in this array would be a float between 0 and 1,
     * and would decide how much data the ith client would take
     */
    private ArrayList<Double> distributionRatiosByClient;

    /**
     * List of List of doubles where jth column in ith row decides how much of class j would the ith clients get.
     */
    private ArrayList<ArrayList<Double>> distributionRatiosByLabels;

    /**
     * If clients should be dropped
     */
    private Boolean useDropping;

    /**
     * List of size rounds, where the number at the ith index represents number of clients to be dropped at ith round
     */
    private List<Integer> dropping;


}
