package testbed.networks;

import testbed.stats.StatCollector;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

public class StatCollectInputStream extends InputStream {
    private final InputStream _inputStream;
    private StatCollector _statCollector;

    public StatCollectInputStream(InputStream inputStream, StatCollector statCollector) {
        _inputStream = inputStream;
        _statCollector = statCollector;
    }

    @Override
    public int read() throws IOException {
        LocalDateTime start = LocalDateTime.now();
        int c = _inputStream.read();
        LocalDateTime end = LocalDateTime.now();

        // TODO: add measured duration to stat collector here

        return c;
    }

    @Override
    public int available() throws IOException {
        return _inputStream.available();
    }

    @Override
    public void close() throws IOException {
        _inputStream.close();
    }

    @Override
    public synchronized void reset() throws IOException {
        _inputStream.reset();
    }

    @Override
    public synchronized void mark(int readlimit) {
        _inputStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return _inputStream.markSupported();
    }

    @Override
    public long skip(long n) throws IOException {
        return _inputStream.skip(n);
    }
}
