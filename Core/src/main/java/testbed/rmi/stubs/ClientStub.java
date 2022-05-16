package testbed.rmi.stubs;

import core.gateways.ClientGateway;
import core.models.TrainingReport;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientStub extends ClientGateway, Remote {
    boolean register() throws RemoteException;
    void healthCheck() throws RemoteException;
    void trainingReport(TrainingReport trainingReport) throws RemoteException;
}
