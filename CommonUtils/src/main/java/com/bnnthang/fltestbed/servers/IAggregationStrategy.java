package com.bnnthang.fltestbed.servers;

import com.bnnthang.fltestbed.models.TrainingReport;

import java.util.List;

public interface IAggregationStrategy {
    /**
     * Implementation for the aggregation strategy.
     * @param trainingReports training reports from clients
     * @throws Exception if problems happen while aggregating
     */
    void aggregate(List<TrainingReport> trainingReports) throws Exception;
}
