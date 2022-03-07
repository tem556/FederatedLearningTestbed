package org.example;

import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.deeplearning4j.datasets.iterator.impl.Cifar10DataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.evaluation.IEvaluation;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.List;
import java.util.stream.Collectors;

public class TrainCoordinatorThread extends Thread {
    private List<ClientTrainThread> clients;
    private AggregationStrategy aggregationStrategy;
    private String pathToBaseModel;
    private String pathToNewModel;
    public IEvaluation evaluation;

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

            // aggregate results
            List<INDArray> results = clients
                    .stream().map(x -> x.weightUpdates)
                    .collect(Collectors.toList());
            INDArray aggResult = aggregationStrategy.aggregate(results);

            // produce the new model
            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(pathToBaseModel);
            model.getLayer(0).setParams(aggResult);

            // evaluate new model
            System.out.println("evaluating...");
            Cifar10DataSetIterator cifarEval = new Cifar10DataSetIterator(96, new int[]{32, 32}, DataSetType.TEST, null, 123L);
            evaluation = model.evaluate(cifarEval);

            ModelSerializer.writeModel(model, pathToNewModel, true);
            System.out.println("wrote new model");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
