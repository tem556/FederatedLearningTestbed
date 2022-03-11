package com.bnnthang.fltestbed.Server;

import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.deeplearning4j.datasets.iterator.impl.Cifar10DataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.IEvaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.common.util.SerializationUtils;

import java.util.List;
import java.util.stream.Collectors;

public class TrainCoordinatorThread extends Thread {
    private List<ClientTrainThread> clients;
    private AggregationStrategy aggregationStrategy;
    private String pathToBaseModel;
    private String pathToNewModel;

    public IEvaluation evaluation;
    public long avgTrainingTimeInSecs = 0;

    TrainCoordinatorThread(List<ClientTrainThread> clients,
                           AggregationStrategy aggregationStrategy,
                           String pathToBaseModel,
                           String pathToNewModel) {
        super();

        this.clients = clients;
        this.aggregationStrategy = aggregationStrategy;
        this.pathToBaseModel = pathToBaseModel;
        this.pathToNewModel = pathToNewModel;

        this.evaluation = null;
        this.avgTrainingTimeInSecs = 0L;
    }

    @Override
    public void run() {
        try {
            // start training
            for (ClientTrainThread client : clients) {
                client.start();
            }

            // wait for result
            for (ClientTrainThread client : clients) {
                client.join();
            }

            // aggregate weight updates and produce the new model
            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(pathToBaseModel);
            for (int layerIndex = 0; layerIndex < model.getnLayers(); ++layerIndex) {
                int finalI = layerIndex;
                List<INDArray> results = clients
                        .stream().map(x -> {
                            return SerializationUtils.<INDArray>fromByteArray(x.trainResult.layerParams.get(finalI));
                        })
                        .collect(Collectors.toList());
                if (results.get(0) != null) {
                    INDArray aggResult = aggregationStrategy.aggregate(results);
                    model.getLayer(layerIndex).setParams(aggResult);
                }
            }

            // evaluate new model
            System.out.println("evaluating...");
            Cifar10DataSetIterator cifarEval = new Cifar10DataSetIterator(96, new int[]{32, 32},
                    DataSetType.TEST, null, 123L);
            evaluation = model.evaluate(cifarEval);

            // average training time
            avgTrainingTimeInSecs = clients.stream().map(x -> x.trainResult.trainingTime).reduce(0L, Long::sum) / clients.size();

            ModelSerializer.writeModel(model, pathToNewModel, true);
            System.out.println("wrote new model");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
