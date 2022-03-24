package com.bnnthang.fltestbed.commonutils.enums;

/**
 * Commands for server to send or request information from clients.
 */
public enum ClientCommandEnum {
    /**
     * Server adds client to training queue.
     */
    ACCEPTED,

    /**
     * Server rejects client.
     */
    REJECTED,

    /**
     * Server pushes model to client.
     */
    MODELPUSH,

    /**
     * Server pushes dataset to client.
     */
    DATASETPUSH,

    /**
     * Server initiates training process at client.
     */
    TRAIN,

    /**
     * Server polls if client finishes training.
     */
    ISTRAINING,

    /**
     * Server requests training report (only if training is done).
     */
    REPORT,

    /**
     * Server closes the connection with client.
     */
    DONE
}
