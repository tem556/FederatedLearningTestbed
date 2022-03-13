package com.bnnthang.fltestbed.Server;

import java.io.IOException;

public interface ClientHandler {
    void register() throws IOException;
    void reject() throws IOException;
    void pushModel() throws IOException, InterruptedException;
    TrainResult train() throws IOException, InterruptedException;
    void done() throws IOException;
    void pushData();
}
