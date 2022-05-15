package core.gateways;

import core.models.TrainingReport;

import java.io.IOException;

public interface ClientGateway {
    boolean register() throws IOException;
    void healthCheck() throws IOException;
    void trainingReport(TrainingReport trainingReport) throws IOException;
}
