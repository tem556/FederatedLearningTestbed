package com.bnnthang.fltestbed.commonutils.servers;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bnnthang.fltestbed.commonutils.models.TrainingReport;

public class EvaluationRunnable implements Runnable {
    /**
     * Server Operations.
     */
    IServerOperations _serverOperations;

    /**
     * Training reports from clients.
     */
    List<TrainingReport> _reports;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationRunnable.class);

    public EvaluationRunnable(IServerOperations serverOperations, List<TrainingReport> reports) {
        _serverOperations = serverOperations;
        _reports = reports;
    }

    @Override
    public void run() {
        try {
            _serverOperations.evaluateCurrentModel(_reports);
        } catch (IOException e) {
            LOGGER.error("EXCEPTION", e);
        }
    }
}
