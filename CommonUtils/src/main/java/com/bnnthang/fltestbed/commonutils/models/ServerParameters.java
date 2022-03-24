package com.bnnthang.fltestbed.commonutils.models;

import com.bnnthang.fltestbed.commonutils.servers.IServerOperations;
import lombok.Data;
import lombok.NonNull;

/**
 * Encapsulate various federated learning server parameters.
 */
@Data
public class ServerParameters {
    /**
     * The port the server listens to.
     */
    @NonNull
    private Integer port;

    /**
     * Configuration for training.
     */
    @NonNull
    private TrainingConfiguration trainingConfiguration;

    /**
     * Detailed implementation for operations.
     */
    @NonNull
    private IServerOperations serverOperations;
}
