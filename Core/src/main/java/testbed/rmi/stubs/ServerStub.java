package testbed.rmi.stubs;

import core.models.ModelUpdate;
import core.models.TrainingParameter;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerStub extends Remote {
    void sendModel(byte[] model) throws RemoteException, IOException;
    void sendModelUpdate(ModelUpdate modelUpdateRequest) throws RemoteException;
    void train(TrainingParameter trainingParameter) throws RemoteException;
}
