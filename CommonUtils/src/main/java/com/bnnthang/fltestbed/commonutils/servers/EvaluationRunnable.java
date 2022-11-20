package com.bnnthang.fltestbed.commonutils.servers;

import java.io.IOException;
import java.util.List;

import com.bnnthang.fltestbed.commonutils.models.TrainingReport;

public class EvaluationRunnable implements Runnable {
    IServerOperations _serverOperations;
    List<TrainingReport> _reports;

    public EvaluationRunnable(IServerOperations serverOperations, List<TrainingReport> reports) {
        _serverOperations = serverOperations;
        _reports = reports;
    }

    @Override
    public void run() {
        try {
            _serverOperations.evaluateCurrentModel(_reports);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
