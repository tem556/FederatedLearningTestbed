package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.clients.IClientNetworkStatManager;
import com.bnnthang.fltestbed.commonutils.clients.IClientTrainingStatManager;
import com.bnnthang.fltestbed.commonutils.models.ModelUpdate;
import com.bnnthang.fltestbed.commonutils.models.ServerParameters;
import com.opencsv.CSVWriter;
import lombok.Getter;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.evaluation.classification.Evaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BaseServerOperations implements IServerOperations {
    /**
     * Logger.
     */
    private static final Logger _logger = LoggerFactory.getLogger(BaseServerOperations.class);

    protected final IServerLocalRepository localRepository;

    protected BaseTrainingIterator trainingIterator;

    @Getter
    protected final List<IClientHandler> acceptedClients;

    protected CSVWriter _logWriter;

    public BaseServerOperations(IServerLocalRepository _localRepository) throws IOException {
        localRepository = _localRepository;
        trainingIterator = null;
        acceptedClients = new ArrayList<>();

        File evalLogFile = new File(localRepository.getLogFolder(), "eval-log.csv");
        evalLogFile.createNewFile();
        _logWriter = new CSVWriter(new FileWriter(evalLogFile));
        _logWriter.writeNext(new String[] {"accuracy", "precision", "f1", "recall"});
    }

    @Override
    public void acceptClient(Socket socket) throws IOException {
        IClientHandler clientHandler = new BaseClientHandler(acceptedClients.size(), socket, localRepository.getLogFolder());
        clientHandler.accept();
        acceptedClients.add(clientHandler);
    }

    @Override
    public void rejectClient(Socket socket) throws IOException {
        IClientHandler clientHandler = new BaseClientHandler(acceptedClients.size(), socket, localRepository.getLogFolder());
        clientHandler.reject();
    }

    @Override
    public void pushDatasetToClients(List<IClientHandler> clients, float ratio) throws IOException {
        _logger.debug("splitting dataset");

        // TODO: not to do this in memory
        List<byte[]> partitions = localRepository.partitionAndSerializeDataset(acceptedClients.size(), ratio);

        _logger.debug("pushing to client");

        // TODO: parallelize this
        for (int i = 0; i < clients.size(); ++i) {
            // send to client
            clients.get(i).pushDataset(partitions.get(i));
        }

        _logger.debug("pushed to all clients");
    }

    @Override
    public void pushModelToClients(List<IClientHandler> clients) throws IOException {
        // TODO: not to load model to memory
        byte[] modelBytes = localRepository.loadAndSerializeLatestModel();
        byte[] weightBytes = localRepository.loadAndSerializeLatestModelWeights();
        for (IClientHandler client : clients) {
            client.pushModel(client.hasLocalModel() ? weightBytes : modelBytes);
        }

        _logger.debug("pushed model to all clients");
    }

    @Override
    public Boolean trainOrElse(ServerParameters serverParameters) throws IOException {
        if (acceptedClients.size() >= serverParameters.getTrainingConfiguration().getMinClients()) {
            _logger.debug("triggered training");

            trainingIterator = new BaseTrainingIterator(
                    this,
                    acceptedClients,
                    serverParameters.getTrainingConfiguration(),
                    new ServerTimeTracker());
            trainingIterator.start();

            return true;
        } else {
            _logger.debug("not good for training");
            return false;
        }
    }

    @Override
    public void aggregateResults(List<ModelUpdate> modelUpdates, IAggregationStrategy aggregationStrategy) throws Exception {
        MultiLayerNetwork currentModel = localRepository.loadLatestModel();
        MultiLayerNetwork newModel = aggregationStrategy.aggregate(currentModel, modelUpdates);
        localRepository.saveNewModel(newModel);
        newModel.close();
        currentModel.close();
    }

    @Override
    public Boolean isTraining() {
        return trainingIterator != null && trainingIterator.isAlive();
    }

    @Override
    public void evaluateCurrentModel(List<ModelUpdate> trainingReports) throws IOException {
        Evaluation evaluation = localRepository.evaluateCurrentModel();
        _logWriter.writeNext(new String[] {
                String.valueOf(evaluation.accuracy()),
                String.valueOf(evaluation.precision()),
                String.valueOf(evaluation.recall()),
                String.valueOf(evaluation.f1()),
        });
        _logWriter.flush();
    }

    @Override
    public void done() throws Exception {
        _logWriter.close();

        // get final report from each client
        for (IClientHandler clientHandler : acceptedClients) {
            clientHandler.done();
        }
    }
}
