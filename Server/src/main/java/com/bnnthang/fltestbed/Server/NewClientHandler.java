package com.bnnthang.fltestbed.Server;

import com.bnnthang.fltestbed.enums.ClientCommandEnum;
import com.bnnthang.fltestbed.models.TrainingReport;
import com.bnnthang.fltestbed.network.SocketUtils;
import com.bnnthang.fltestbed.servers.IClientHandler;
import com.google.common.primitives.Ints;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.shade.guava.primitives.Booleans;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class NewClientHandler implements IClientHandler {
    private final Socket socket;
    private boolean hasLocalModel = false;

    public NewClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public Boolean isTraining() {
        try {
            System.out.println("checking client training...");
            SocketUtils.sendInteger(socket, ClientCommandEnum.ISTRAINING.ordinal());
            int flag = SocketUtils.readInteger(socket);
            return flag != 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void pushDataset(byte[] bytes) throws Exception {
        SocketUtils.sendInteger(socket, ClientCommandEnum.DATASETPUSH.ordinal());
        SocketUtils.sendBytesWrapper(socket, bytes);
    }

    @Override
    public void pushModel(byte[] bytes) throws Exception {
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
    public void startTraining() throws Exception {
        SocketUtils.sendInteger(socket, ClientCommandEnum.TRAIN.ordinal());

//        do {
//            Thread.sleep(5000);
//        } while (isTraining());
    }

    @Override
    public TrainingReport getTrainingReport() {
        try {
            SocketUtils.sendInteger(socket, ClientCommandEnum.REPORT.ordinal());
            int length = SocketUtils.readInteger(socket);
            System.out.println("got report length = " + length);
            byte[] bytes = socket.getInputStream().readNBytes(length);
//            byte[] bytes = SocketUtils.readBytes(socket, length);
//            return SerializationUtils.deserialize(bytes);
//            byte[] bytes = SocketUtils.readBytesWrapper(socket);
            TrainingReport report = SerializationUtils.deserialize(bytes);
            return report;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void done() throws Exception {
        SocketUtils.sendInteger(socket, ClientCommandEnum.DONE.ordinal());
        socket.close();
    }

    @Override
    public void accept() throws Exception {
        // send accepted message
        SocketUtils.sendInteger(socket, ClientCommandEnum.ACCEPTED.ordinal());

        // check if client has local model
        hasLocalModel = SocketUtils.readInteger(socket) == 1;
    }

    @Override
    public void reject() throws Exception {
        SocketUtils.sendInteger(socket, ClientCommandEnum.REJECTED.ordinal());
    }
}
