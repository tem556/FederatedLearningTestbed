package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.enums.ClientCommandEnum;
import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import com.bnnthang.fltestbed.commonutils.utils.SocketUtils;
import org.nd4j.common.util.SerializationUtils;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseClientHandler implements IClientHandler {
    private final Socket socket;
    private boolean hasLocalModel;

    private Double uplinkTime;

    public BaseClientHandler(Socket _socket) {
        socket = _socket;
        hasLocalModel = false;
    }

    @Override
    public void accept() throws IOException {
        // send accepted message
        SocketUtils.sendInteger(socket, ClientCommandEnum.ACCEPTED.ordinal());

        // check if client has local model
        // TODO: read 1 byte only
        hasLocalModel = SocketUtils.readInteger(socket) == 1;
    }

    @Override
    public void reject() throws IOException {
        SocketUtils.sendInteger(socket, ClientCommandEnum.REJECTED.ordinal());
    }

    @Override
    public void pushDataset(byte[] bytes) throws IOException {
        // TODO: think of an alternative to send dataset
        SocketUtils.sendInteger(socket, ClientCommandEnum.DATASETPUSH.ordinal());
        SocketUtils.sendBytesWrapper(socket, bytes);
    }

    @Override
    public void pushModel(byte[] bytes) throws IOException {
        if (hasLocalModel) {
            // TODO: send weight updates
        } else {
            System.out.println("pushing");
            SocketUtils.sendInteger(socket, ClientCommandEnum.MODELPUSH.ordinal());
            SocketUtils.sendBytesWrapper(socket, bytes);
            System.out.println("pushed model length = " + bytes.length);
        }
    }

    @Override
    public void startTraining() throws IOException {
        SocketUtils.sendInteger(socket, ClientCommandEnum.TRAIN.ordinal());

        // reset
        uplinkTime = -1.0;
    }

    @Override
    public TrainingReport getTrainingReport() throws IOException {
        SocketUtils.sendInteger(socket, ClientCommandEnum.REPORT.ordinal());

        LocalDateTime startTime = LocalDateTime.now();
        byte[] bytes = SocketUtils.readBytesWrapper(socket);
        LocalDateTime endTime = LocalDateTime.now();
        uplinkTime = (double) Duration.between(startTime, endTime).getSeconds() / bytes.length;

        return SerializationUtils.deserialize(bytes);
    }

    @Override
    public void done() throws IOException {
        SocketUtils.sendInteger(socket, ClientCommandEnum.DONE.ordinal());
        socket.close();
    }

    @Override
    public Boolean isTraining() {
        boolean res = false;
        try {
            SocketUtils.sendInteger(socket, ClientCommandEnum.ISTRAINING.ordinal());
            // TODO: read 1 byte only
            int flag = SocketUtils.readInteger(socket);
            res = flag != 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Double getUplinkTime() {
        return uplinkTime;
    }
}
