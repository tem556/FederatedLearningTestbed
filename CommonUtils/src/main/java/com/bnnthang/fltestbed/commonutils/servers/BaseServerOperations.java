package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.models.ServerParameters;
import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import lombok.Getter;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BaseServerOperations implements IServerOperations {
    private IServerLocalRepository localRepository;
    private BaseTrainingIterator trainingIterator;

    @Getter
    private List<IClientHandler> acceptedClients;

    public BaseServerOperations(IServerLocalRepository _localRepository) {
        localRepository = _localRepository;
        trainingIterator = null;
        acceptedClients = new ArrayList<>();
    }

    @Override
    public void acceptClient(Socket socket) throws IOException {
        IClientHandler clientHandler = new BaseClientHandler(socket);
        clientHandler.accept();
        acceptedClients.add(clientHandler);
    }

    @Override
    public void rejectClient(Socket socket) throws IOException {
        IClientHandler clientHandler = new BaseClientHandler(socket);
        clientHandler.reject();
    }

    @Override
    public void pushDatasetToClients(List<IClientHandler> clients) throws IOException {
        // TODO: not to do this in memory
        System.out.println("spliting dataset");
        List<byte[]> partitions = localRepository.partitionAndSerializeDataset(acceptedClients.size());
        System.out.println("pushing to clients");
        // TODO: parallelize this
        for (int i = 0; i < clients.size(); ++i) {
            // send to client
            clients.get(i).pushDataset(partitions.get(i));
        }
        System.out.println("pushed all clients");
    }

    @Override
    public void pushModelToClients(List<IClientHandler> clients) throws IOException {
        // TODO: not to load model to memory
        byte[] bytes = localRepository.loadAndSerializeLatestModel();
        for (IClientHandler client : clients) {
            client.pushModel(bytes);
        }
        System.out.println("pushed model to all clients");
    }

    @Override
    public void trainOrElse(ServerParameters serverParameters) {
        if (acceptedClients.size() >= serverParameters.getTrainingConfiguration().getMinClients()) {
            System.out.println("triggered training");
            trainingIterator = new BaseTrainingIterator(this, acceptedClients, serverParameters.getTrainingConfiguration());
            trainingIterator.start();
        } else {
            System.out.println("not good for training");
        }
    }

    @Override
    public void aggregateResults(List<TrainingReport> trainingReports, IAggregationStrategy aggregationStrategy) throws Exception {
        MultiLayerNetwork currentModel = localRepository.loadLatestModel();
        MultiLayerNetwork newModel = aggregationStrategy.aggregate(currentModel, trainingReports);
        localRepository.saveNewModel(newModel);
    }

    @Override
    public Boolean isTraining() {
        return trainingIterator != null && trainingIterator.isAlive();
    }
}
