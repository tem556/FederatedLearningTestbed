package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.enums.ClientCommandEnum;
import com.bnnthang.fltestbed.commonutils.models.TrainingReport;
import com.bnnthang.fltestbed.commonutils.utils.SocketUtils;
import com.bnnthang.fltestbed.commonutils.utils.TimeUtils;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import org.nd4j.common.util.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;

public class BaseClientHandler implements IClientHandler {
    private static final Logger _logger = LoggerFactory.getLogger(BaseClientHandler.class);

    protected int _id;

    protected final Socket _socket;

    protected boolean localModel;

    protected ICSVWriter _logWriter;

    public BaseClientHandler(int id, Socket socket, File logFolder) throws IOException {
        _id = id;
        _socket = socket;
        localModel = false;

        File logFile = new File(logFolder, "client" + _id + "-log.csv");
        if (!logFile.exists()) {
            if (logFile.createNewFile()) {
                _logWriter = new CSVWriter(new FileWriter(logFile));
                _logWriter.writeNext(new String[] {
                        "training time (ms)",
                        "uplink bytes (bytes)",
                        "uplink time (ms)",
                        "downlink bytes (bytes)",
                        "downlink time (ms)"
                });
            } else {
                throw new IOException("cannot create client log file");
            }
        } else {
            _logWriter = new CSVWriter(new FileWriter(logFile));
        }
    }

    @Override
    public void accept() throws IOException {
        // send accepted message
        SocketUtils.sendInteger(_socket, ClientCommandEnum.ACCEPTED.ordinal());

        // check if client has local model
        // TODO: read 1 byte only
        localModel = SocketUtils.readInteger(_socket) == 1;
    }

    @Override
    public void reject() throws IOException {
        SocketUtils.sendInteger(_socket, ClientCommandEnum.REJECTED.ordinal());
    }

    @Override
    public void pushDataset(byte[] bytes) throws IOException {
        // TODO: think of an alternative to send dataset
        SocketUtils.sendInteger(_socket, ClientCommandEnum.DATASETPUSH.ordinal());
        SocketUtils.sendBytesWrapper(_socket, bytes);

        _logger.debug("pushed dataset length = " + bytes.length);
    }

    @Override
    public void pushModel(byte[] bytes) throws IOException {
        _logger.debug(localModel ? "pushing weights" : "pushing");

        localModel = true;
        SocketUtils.sendInteger(_socket, ClientCommandEnum.MODELPUSH.ordinal());
        SocketUtils.sendBytesWrapper(_socket, bytes);

        _logger.debug("pushed model length = " + bytes.length);
    }

    @Override
    public void startTraining() throws IOException {
        SocketUtils.sendInteger(_socket, ClientCommandEnum.TRAIN.ordinal());
    }

    @Override
    public TrainingReport getTrainingReport() throws IOException {
        SocketUtils.sendInteger(_socket, ClientCommandEnum.REPORT.ordinal());

        LocalDateTime startTime = LocalDateTime.now();
        byte[] bytes = SocketUtils.readBytesWrapper(_socket);
        LocalDateTime endTime = LocalDateTime.now();

        TrainingReport report = SerializationUtils.deserialize(bytes);

        report.getMetrics().setUplinkBytes((long) bytes.length);
        report.getMetrics().setUplinkTime(TimeUtils.millisecondsBetween(startTime, endTime));
        _logWriter.writeNext(report.getMetrics().toCsvLine());

        return report;
    }

    @Override
    public void done() throws IOException {
        SocketUtils.sendInteger(_socket, ClientCommandEnum.DONE.ordinal());
        _socket.close();
        _logWriter.close();
    }

    @Override
    public Boolean isTraining() {
        boolean res = false;
        try {
            SocketUtils.sendInteger(_socket, ClientCommandEnum.ISTRAINING.ordinal());
            // TODO: read 1 byte only
            int flag = SocketUtils.readInteger(_socket);
            res = flag != 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public Boolean isAlive() {
        return null;
    }

    @Override
    public Boolean hasLocalModel() {
        return localModel;
    }
}
