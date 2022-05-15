package core.workers;

import core.gateways.ClientGateway;
import core.models.TrainingParameter;
import core.models.TrainingReport;
import core.repositories.ClientRepository;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;

public abstract class BaseWorker extends Thread {
    TrainingParameter  _trainingParameter;
    ClientRepository _clientRepository;
    ClientGateway _clientGateway;

    public BaseWorker(TrainingParameter trainingParameter,
                      ClientRepository clientRepository,
                      ClientGateway clientGateway) {
        _trainingParameter = trainingParameter;
        _clientRepository = clientRepository;
        _clientGateway = clientGateway;
    }

    @Override
    public void run() {
        try {
            DataSetIterator iterator = getDataSetIteratorFromRepository();

            Nd4j.getMemoryManager().togglePeriodicGc(false);

            MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(_clientRepository.getModel());
            model.fit(iterator, _trainingParameter.getEpochs());

            sendReport(model);

            model.close();
        } catch (IOException exception) {

        }
    }

    protected abstract DataSetIterator getDataSetIteratorFromRepository();

    protected void sendReport(Model model) throws IOException {
        INDArray newModelParameter = model.params().dup();

        // create report
        TrainingReport report = new TrainingReport();
        report.setModelParameter(newModelParameter);

        // send report
        _clientGateway.trainingReport(report);

        newModelParameter.close();
    }
}
