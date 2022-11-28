package com.bnnthang.fltestbed.Client;

import com.beust.jcommander.Parameter;

public class AppArgs {
    @Parameter(names = "--host", description = "Server host address")
    public String host = "127.0.0.1";

    @Parameter(names = "--port", description = "Port for communication")
    public Integer port = 4602;

    @Parameter(names = "--numClients", description = "Number of clients participating")
    public Integer numClients = 1;
    
    @Parameter(names = "--workdir", description = "Working directory where the server saves the model and the training results")
    public String workDir = System.getProperty("user.dir");

    @Parameter(names = "--healthdataset", description = "Set to true if the health dataset is to be used, otherwise cifar-10 dataset will be used")
    public Boolean useHealthDataset = false;

    @Parameter(names = "--batchsize", description = "Batch size to be used for training")
    public Integer batchSize = 16;

    @Parameter(names = "--fl")
    public Boolean fl = false;

    @Parameter(names = "--ml")
    public Boolean ml = false;
}
