package com.bnnthang.fltestbed.Server;

import com.bnnthang.fltestbed.Server.Repositories.Cifar10Repository;
import com.bnnthang.fltestbed.models.ServerParameters;
import com.bnnthang.fltestbed.models.TrainingReport;
import com.bnnthang.fltestbed.servers.BaseTrainingIterator;
import com.bnnthang.fltestbed.servers.IAggregationStrategy;
import com.bnnthang.fltestbed.servers.IClientHandler;
import com.bnnthang.fltestbed.servers.IServerOperations;
import org.apache.commons.lang3.tuple.Pair;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ServerOperations implements IServerOperations {
//    public static String MODEL_PATH = "C:/Users/buinn/DoNotTouch/crap/photolabeller/newmodel.zip";

    public String workDir = null;
    public String currentModelFilename = null;
    private BaseTrainingIterator trainingIteration = null;

    public ServerOperations(String _workDir, String baseModelFilename) {
        workDir = _workDir;
        currentModelFilename = baseModelFilename;
    }

    @Override
    public Boolean isTraining() {
        return trainingIteration != null && trainingIteration.isAlive();
    }

    @Override
    public void acceptClient(Socket socket, List<IClientHandler> clients) throws Exception {
        NewClientHandler clientHandler = new NewClientHandler(socket);
        clientHandler.accept();
        clients.add(clientHandler);
        System.out.println("accepted a client");
    }

    @Override
    public void rejectClient(Socket socket) throws Exception {
        NewClientHandler clientHandler = new NewClientHandler(socket);
        clientHandler.reject();
    }

    @Override
    public void pushDatasetToClients(List<IClientHandler> clients) throws Exception {
        System.out.println("spliting dataset");
        Cifar10Repository repository = new Cifar10Repository();
        List<List<byte[]>> partitions = repository.splitDatasetIID(clients.size());
        System.out.println("pushing to clients");
        for (int i = 0; i < clients.size(); ++i) {
            // send to client
            clients.get(i).pushDataset(Cifar10Repository.flatten(partitions.get(i)));
        }
        System.out.println("pushed all clients");
    }

    @Override
    public void pushModelToClients(List<IClientHandler> clients) throws Exception {
        System.out.println("pushing model to clients -> " + currentModelFilename);
        File f = new File(workDir, currentModelFilename);
        int modelLength = (int)f.length();
        FileInputStream fis = new FileInputStream(f);
        byte[] bytes = new byte[modelLength];
        fis.read(bytes, 0, modelLength);

        for (IClientHandler client : clients) {
            client.pushModel(bytes);
        }
        System.out.println("pushed model to all clients");
    }

    @Override
    public void trainOrElse(List<IClientHandler> clients, ServerParameters serverParameters) throws Exception {
        if (clients.size() >= serverParameters.getTrainingConfiguration().getMinClients()) {
            System.out.println("triggered training");
            trainingIteration = new BaseTrainingIterator(this, clients, serverParameters.getTrainingConfiguration());
            trainingIteration.start();
        } else {
            System.out.println("not good for training");
        }
    }

    @Override
    public void aggregateResults(List<TrainingReport> reports, IAggregationStrategy aggregationStrategy) throws Exception {
        System.out.println("aggregating results");
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(workDir + "/" + currentModelFilename);
        MultiLayerNetwork newModel = aggregationStrategy.aggregate(model, reports);
        String newModelFilename = "model" + (new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss").format(Calendar.getInstance().getTime()));
        newModel.save(new File(workDir, newModelFilename));
        currentModelFilename = newModelFilename;
    }
}
