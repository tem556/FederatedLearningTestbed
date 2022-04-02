package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.models.ServerParameters;
import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.evaluation.classification.Evaluation;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BaseServerOperations implements IServerOperations {
    /**
     * Logger.
     */
    private static final Logger _logger = LogManager.getLogger(BaseServerOperations.class);

    private final IServerLocalRepository localRepository;

    private BaseTrainingIterator trainingIterator;

    @Getter
    private final List<IClientHandler> acceptedClients;

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

            // create result file
            localRepository.createNewResultFile();

            trainingIterator = new BaseTrainingIterator(this, acceptedClients, serverParameters.getTrainingConfiguration());
            trainingIterator.start();

            return true;
        } else {
            _logger.debug("not good for training");
            return false;
        }
    }

    @Override
    public void aggregateResults(List<TrainingReport> trainingReports, IAggregationStrategy aggregationStrategy) throws Exception {
        MultiLayerNetwork currentModel = localRepository.loadLatestModel();
        MultiLayerNetwork newModel = aggregationStrategy.aggregate(currentModel, trainingReports);
        localRepository.saveNewModel(newModel);
        newModel.close();
        currentModel.close();
    }

    @Override
    public Boolean isTraining() {
        return trainingIterator != null && trainingIterator.isAlive();
    }

    @Override
    public void evaluateCurrentModel(List<TrainingReport> trainingReports) throws IOException {
        Evaluation evaluation = localRepository.evaluateCurrentModel();

        // calculate avg training time
        double sumTrainingTime = trainingReports.stream().map(TrainingReport::getTrainingTime).reduce(0.0, Double::sum);
        double avgTrainingTime = sumTrainingTime / acceptedClients.size();

        // calculate avg uplink time
        double sumUplinkTime = acceptedClients.stream().map(IClientHandler::getUplinkTime).reduce(0.0, Double::sum);
        double avgUplinkTime = sumUplinkTime / acceptedClients.size();

        // calculate avg downlink time
        double sumDownlinkTime = trainingReports.stream().map(TrainingReport::getDownlinkTime).reduce(0.0, Double::sum);
        double avgDownlinkTime = sumDownlinkTime / acceptedClients.size();

        // calculate avg power consumption
        double sumPower = trainingReports.stream().map(x -> x.getCommunicationPower().getPowerConsumption()).reduce(0.0, Double::sum);
        double avgPower = sumPower / acceptedClients.size();

        // TODO: move this to repo logic
        // accuracy, precision, recall, f1, training time (ms), downlink time (ms), uplink time (ms), power (j)
        String evalString = String.format("%f,%f,%f,%f,%f,%f,%f,%f\n",
                evaluation.accuracy(),
                evaluation.precision(),
                evaluation.recall(),
                evaluation.f1(),
                avgTrainingTime,
                avgDownlinkTime,
                avgUplinkTime,
                avgPower);
        localRepository.appendToCurrentFile(evalString);
    }
}
