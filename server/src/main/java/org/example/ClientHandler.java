package org.example;

import java.io.IOException;

public interface ClientHandler {
    public void register() throws IOException;
    public void reject();
    public void pushModel() throws IOException;
    public byte[] train() throws IOException;
    public void close() throws IOException;
}
