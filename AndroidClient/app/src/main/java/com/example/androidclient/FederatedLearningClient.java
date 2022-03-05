package com.example.androidclient;

import java.io.IOException;

public interface FederatedLearningClient {
    public void register() throws IOException;
    public void train() throws IOException;
}
