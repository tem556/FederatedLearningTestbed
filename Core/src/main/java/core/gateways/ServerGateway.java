package core.gateways;

import core.models.ModelUpdate;
import core.models.TrainingParameter;

import java.io.File;
import java.io.IOException;

public interface ServerGateway {
    void sendModel(File model) throws IOException;
    void sendModelUpdate(ModelUpdate modelUpdateRequest) throws IOException;
    void train(TrainingParameter trainingParameter) throws IOException;
}
