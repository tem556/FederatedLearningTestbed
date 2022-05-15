package core.aggregationstrategies;

import core.models.TrainingReport;
import org.deeplearning4j.nn.api.Model;

import java.util.List;

public interface AggregationStrategy {
    void aggregate(Model currentModel, List<TrainingReport> reports);
}
