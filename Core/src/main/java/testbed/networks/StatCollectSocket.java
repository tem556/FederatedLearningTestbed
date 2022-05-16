package testbed.networks;

import testbed.stats.StatCollector;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class StatCollectSocket extends Socket {
    private Socket _socket;
    private StatCollector _statCollector;

    public StatCollectSocket(Socket socket, StatCollector statCollector) throws IOException {
        _socket = socket;
        _statCollector = statCollector;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new StatCollectInputStream(super.getInputStream(), _statCollector);
    }
}
