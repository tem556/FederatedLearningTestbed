package com.bnnthang.fltestbed.commonutils.models;

import lombok.Data;
import lombok.NonNull;

@Data
public class ClientParameters {
    @NonNull
    private String serverHost;

    @NonNull
    private Integer serverPort;

    @NonNull
    private String workDirectory;

    @NonNull
    private Integer numClients;

    @NonNull
    private Double mflopsPerRound;
}
