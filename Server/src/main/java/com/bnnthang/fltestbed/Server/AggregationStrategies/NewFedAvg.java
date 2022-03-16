package com.bnnthang.fltestbed.Server.AggregationStrategies;

import com.bnnthang.fltestbed.Server.ServerOperations;
import com.bnnthang.fltestbed.models.TrainingReport;
import com.bnnthang.fltestbed.servers.IAggregationStrategy;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.List;

public class NewFedAvg implements IAggregationStrategy {
    @Override
    public MultiLayerNetwork aggregate(MultiLayerNetwork model, List<TrainingReport> reports) throws Exception {
        int layers = reports.get(0).getLayerParams().size();
        for (int i = 0; i < layers; ++i) {
            if (reports.get(0).getLayerParams().get(i) == null) {
                continue;
            }

            INDArray identity = Nd4j.zeros(reports.get(0).getLayerParams().get(i).shape());
            int finalI = i;
            INDArray sumUpdates = reports.stream().map(x -> x.getLayerParams().get(finalI)).reduce(identity, INDArray::add);
            INDArray avgUpdates = sumUpdates.div(reports.size());
            model.getLayer(i).setParams(avgUpdates);
        }

        return model;

//        // overwrite new model
//        ModelSerializer.writeModel(model, ServerOperations.MODEL_PATH, true);
    }
}
