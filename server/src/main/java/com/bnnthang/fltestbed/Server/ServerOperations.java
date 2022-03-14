package com.bnnthang.fltestbed.Server;

import com.bnnthang.fltestbed.Server.Repositories.Cifar10Repository;
import com.bnnthang.fltestbed.models.ServerParameters;
import com.bnnthang.fltestbed.models.TrainingReport;
import com.bnnthang.fltestbed.servers.BaseTrainingIterator;
import com.bnnthang.fltestbed.servers.IAggregationStrategy;
import com.bnnthang.fltestbed.servers.IClientHandler;
import com.bnnthang.fltestbed.servers.IServerOperations;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.List;

public class ServerOperations implements IServerOperations {
    public static String MODEL_PATH = "C:/Users/buinn/DoNotTouch/crap/photolabeller/newmodel.zip";

    private BaseTrainingIterator trainingIteration = null;

    @Override
    public Boolean isTraining() {
        return trainingIteration != null && trainingIteration.isAlive();
    }

    @Override
    public void acceptClient(Socket socket, List<IClientHandler> clients) throws Exception {
        NewClientHandler clientHandler = new NewClientHandler(socket);
        clientHandler.accept();
        clients.add(clientHandler);
    }

    @Override
    public void rejectClient(Socket socket) throws Exception {
        NewClientHandler clientHandler = new NewClientHandler(socket);
        clientHandler.reject();
    }

    @Override
    public void pushDatasetToClients(List<IClientHandler> clients) throws Exception {
        Cifar10Repository repository = new Cifar10Repository();
        List<List<Pair<byte[], Byte>>> partitions = repository.splitDatasetIID(clients.size());
        for (int i = 0; i < clients.size(); ++i) {
            // serialize dataset
            byte[] datasetBytes = partitions.get(i).stream().map(x -> {
                byte[] res = new byte[x.getLeft().length + 1];
                res[0] = x.getRight();
                System.arraycopy(x.getLeft(), 0, res, 1, x.getLeft().length);
                return res;
            }).reduce((x, y) -> {
                byte[] res = new byte[x.length + y.length];
                System.arraycopy(x, 0, res, 0, x.length);
                System.arraycopy(y, 0, res, x.length, y.length);
                return res;
            }).orElse(new byte[0]);

            // send to client
            clients.get(i).pushDataset(datasetBytes);
        }
    }

    @Override
    public void pushModelToClients(List<IClientHandler> clients) throws Exception {
        File f = new File(MODEL_PATH);
        int modelLength = (int)f.length();
        FileInputStream fis = new FileInputStream(f);
        byte[] bytes = new byte[modelLength];
        fis.read(bytes, 0, modelLength);

        for (IClientHandler client : clients) {
            client.pushModel(bytes);
        }
    }

    @Override
    public void trainOrElse(List<IClientHandler> clients, ServerParameters serverParameters) throws Exception {
        if (clients.size() >= serverParameters.getTrainingConfiguration().getMinClients()) {
            trainingIteration = new BaseTrainingIterator(this, clients, serverParameters.getTrainingConfiguration());
            trainingIteration.start();
        }
    }

    @Override
    public void aggregateResults(List<TrainingReport> reports, IAggregationStrategy aggregationStrategy) throws Exception {
        aggregationStrategy.aggregate(reports);
    }
}
