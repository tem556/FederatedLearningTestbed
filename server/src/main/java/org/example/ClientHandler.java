package org.example;

import java.io.IOException;

public interface ClientHandler {
    public void register() throws IOException;
    public void reject();
    public void pushModel() throws IOException, InterruptedException;
    public byte[] train() throws IOException, InterruptedException;
    public void close() throws IOException;
}
