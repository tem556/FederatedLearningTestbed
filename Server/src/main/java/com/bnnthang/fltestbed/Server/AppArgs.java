package com.bnnthang.fltestbed.Server;

import com.beust.jcommander.Parameter;

public class AppArgs {
    @Parameter(names = "--port", description = "Port for communication")
    public Integer port = 4602;

    @Parameter(names = "--numClients", description = "Number of clients participating")
    public Integer numClients = 1;
    
    @Parameter(names = "--rounds", description = "Number of training rounds")
    public Integer rounds = 1;
    
    @Parameter(names = "--workdir", description = "Working directory where the server saves the model and the training results")
    public String workDir = System.getProperty("user.dir");
    
    // TODO: add the feature that the server autonomously starts the training in Android devices
    public String apkPath = System.getProperty("user.dir") + "/app-debug.apk";

    @Parameter(names = "--datasetratio", description = "How much of the total dataset that is used for training")
    public Float datasetRatio = 1.0F;

    @Parameter(names = "--config", description = "Set to true if config.json file is to be used, otherwise data will be divided evenly among clients")
    public Boolean useConfig = false;

    @Parameter(names = "--healthdataset", description = "Set to true if the health dataset is to be used, otherwise cifar-10 dataset will be used")
    public Boolean useHealthDataset = false;

    @Parameter(names = "--fl")
    public Boolean fl = false;

    @Parameter(names = "--ml")
    public Boolean ml = false;
}
