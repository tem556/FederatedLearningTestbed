package com.bnnthang.fltestbed.servers;

import com.bnnthang.fltestbed.models.TrainingReport;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.util.List;

public interface IAggregationStrategy {
    /**
     * Implementation for the aggregation strategy.
     * @param trainingReports training reports from clients
     * @throws Exception if problems happen while aggregating
     */
    MultiLayerNetwork aggregate(MultiLayerNetwork model, List<TrainingReport> trainingReports) throws Exception;
}
