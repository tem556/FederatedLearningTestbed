package com.bnnthang.fltestbed.commonutils.servers;

import com.bnnthang.fltestbed.commonutils.clients.IClientNetworkStatManager;
import com.bnnthang.fltestbed.commonutils.clients.IClientTrainingStatManager;
import com.bnnthang.fltestbed.commonutils.enums.ClientCommandEnum;
import com.bnnthang.fltestbed.commonutils.models.ModelUpdate;
import com.bnnthang.fltestbed.commonutils.utils.SocketUtils;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import org.nd4j.common.util.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

public class BaseClientHandler implements IClientHandler {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClientHandler.class);

    /**
     * Client ID.
     */
    protected int _id;

    /**
     * Socket for communication.
     */
    protected final Socket _socket;

    /**
     * Whether the client has a local model.
     */
    protected boolean localModel;

    /**
     * Client's stat log folder.
     */
    protected File _logFolder;

    protected long configurationStartTime;

    public BaseClientHandler(int id, Socket socket, File logFolder) throws IOException {
        _id = id;
        _socket = socket;
        _logFolder = logFolder;
        localModel = false;
        configurationStartTime = -1L;
    }

    @Override
    public void accept() throws IOException {
        // send accepted message
        SocketUtils.sendInteger(_socket, ClientCommandEnum.ACCEPTED.ordinal());

        // check if client has local model
        // TODO: read 1 byte only
        localModel = SocketUtils.readInteger(_socket).getValue() == 1;
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

        LOGGER.debug("pushed dataset length = " + bytes.length);
    }

    @Override
    public void pushModel(byte[] bytes) throws IOException {
        LOGGER.debug(localModel ? "pushing weights" : "pushing");

        localModel = true;

        configurationStartTime = System.currentTimeMillis();
        SocketUtils.sendInteger(_socket, ClientCommandEnum.MODELPUSH.ordinal());
        SocketUtils.sendBytesWrapper(_socket, bytes);

        LOGGER.debug("pushed model length = " + bytes.length);
    }

    @Override
    public void startTraining() throws IOException {
        SocketUtils.sendInteger(_socket, ClientCommandEnum.TRAIN.ordinal());
    }

    @Override
    public ModelUpdate getTrainingReport() throws IOException {
        SocketUtils.sendInteger(_socket, ClientCommandEnum.REPORT.ordinal());
        byte[] bytes = SocketUtils.readBytesWrapper(_socket).getValue();
        ModelUpdate report = SerializationUtils.deserialize(bytes);
        return report;
    }

    @Override
    public void done() throws IOException {
        SocketUtils.sendInteger(_socket, ClientCommandEnum.DONE.ordinal());

        // read final report
        // TODO: check the node dropping experiments
        // client download stat
        IClientNetworkStatManager downloadStat = readAndDeserializeReports();
        // client upload stat
        IClientNetworkStatManager uploadStat = readAndDeserializeReports();
        // training stat
        IClientTrainingStatManager trainingStat = readAndDeserializeReports();
        
        // create file and write headers
        File logFile = new File(_logFolder, "client" + _id + "-log.csv");
        logFile.createNewFile();
        ICSVWriter logWriter = new CSVWriter(new FileWriter(logFile));
        logWriter.writeNext(new String[] {
                "training time (ms)",
                "uplink bytes (bytes)",
                "uplink time (ms)",
                "downlink bytes (bytes)",
                "downlink time (ms)"
        });
        // write log
        Integer rounds = downloadStat.getRounds();
        for (Integer round = 0; round < rounds; ++round) {
            logWriter.writeNext(new String[] {
                trainingStat.getTrainingTime(round).toString(),
                uploadStat.getBytes(round).toString(),
                uploadStat.getCommTime(round).toString(),
                downloadStat.getBytes(round).toString(),
                downloadStat.getCommTime(round).toString()
            });
        }
        logWriter.close();

        // End communication.
        _socket.close();
    }

    @Override
    public Boolean isTraining() {
        boolean res = false;
        try {
            SocketUtils.sendInteger(_socket, ClientCommandEnum.ISTRAINING.ordinal());
            // TODO: read 1 byte only
            int flag = SocketUtils.readInteger(_socket).getValue();
            res = flag != 0;
        } catch (IOException e) {
            LOGGER.error("EXCEPTION", e);
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

    protected <T> T readAndDeserializeReports() throws IOException {
        T report = SerializationUtils.deserialize(SocketUtils.readBytesWrapper(_socket).getValue());
        return report;
    }
}
