package org.example;

import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.deeplearning4j.datasets.iterator.impl.Cifar10DataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.List;
import java.util.stream.Collectors;

public class TrainCoordinatorThread extends Thread {
    private List<ClientTrainThread> clients;
    private AggregationStrategy aggregationStrategy;
    private String pathToBaseModel;
    private String pathToNewModel;

    TrainCoordinatorThread(List<ClientTrainThread> clients,
                           AggregationStrategy aggregationStrategy,
                           String pathToBaseModel,
                           String pathToNewModel) {
        super();
        this.clients = clients;
        this.aggregationStrategy = aggregationStrategy;
        this.pathToBaseModel = pathToBaseModel;
        this.pathToNewModel = pathToNewModel;
    }

    @Override
    public void run() {
        try {
            // start training
            for (int i = 0; i < clients.size(); ++i) {
                clients.get(i).start();
            }

            // wait for result
            for (int i = 0; i < clients.size(); ++i) {
                clients.get(i).join();
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
            Cifar10DataSetIterator cifar = new Cifar10DataSetIterator(96, new int[]{32, 32}, DataSetType.TRAIN, null, 123L);
            Cifar10DataSetIterator cifarEval = new Cifar10DataSetIterator(96, new int[]{32, 32}, DataSetType.TEST, null, 123L);
            EvaluativeListener evaluativeListener = new EvaluativeListener(cifarEval, 1, InvocationType.EPOCH_END);
            evaluativeListener.setCallback(new EvaluationOutput());
            model.setListeners(new ScoreIterationListener(50), evaluativeListener);
            model.fit(cifar);

            ModelSerializer.writeModel(model, pathToNewModel, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
