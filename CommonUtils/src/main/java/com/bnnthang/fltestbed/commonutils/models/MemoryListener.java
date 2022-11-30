package com.bnnthang.fltestbed.commonutils.models;

import org.bytedeco.javacpp.Pointer;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.optimize.api.BaseTrainingListener;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemoryListener extends BaseTrainingListener {
    private static final Logger log = LoggerFactory.getLogger(MemoryListener.class);

    @Override
    public void iterationDone(Model model, int iteration, int epoch) {
        if (iteration % 25 != 0) {
            return;
        }

        log.info("Workspaces = " + Nd4j.getWorkspaceManager().getAllWorkspacesIdsForCurrentThread()  + " Iteration = " + iteration + " Epoch = " + epoch + " Mem = " + Pointer.physicalBytes());
    }
}

