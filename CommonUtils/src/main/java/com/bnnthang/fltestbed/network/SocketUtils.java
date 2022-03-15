package com.bnnthang.fltestbed.network;

import org.nd4j.shade.guava.primitives.Ints;

import java.io.IOException;
import java.net.Socket;

public final class SocketUtils {
    private SocketUtils() { }

    /**
     * Maximum buffer size.
     */
    private static final int BUFFER_SIZE = 2048;

    /**
     * Enhanced wrapper for <code>sendBytes</code>.
     * @param socket some socket
     * @param bytes byte array to send
     * @throws IOException
     */
    public static void sendBytesWrapper(final Socket socket,
                                        final byte[] bytes)
            throws IOException {
        sendInteger(socket, bytes.length);
        sendBytes(socket, bytes);
    }

    /**
     * Enhanced wrapper for <code>readBytes</code>.
     * @param socket some socket
     * @return read buffer
     * @throws IOException
     */
    public static byte[] readBytesWrapper(final Socket socket)
            throws IOException {
        int size = readInteger(socket);
        byte[] bytes = new byte[size];
        byte[] buffer = new byte[BUFFER_SIZE];
        int current = 0;
        while (current < size) {
            // read to buffer
            int readBytes = socket.getInputStream()
                    .read(buffer, 0, Integer.min(BUFFER_SIZE, size - current));

            // copy to result
            System.arraycopy(buffer, 0, bytes, current, readBytes);

            // increase counter
            current += readBytes;
        }
        return bytes;
    }

    /**
     * Send a sequence of bytes via the given socket.
     * @param socket some socket
     * @param bytes byte array to send
     * @throws IOException
     */
    public static void sendBytes(final Socket socket,
                                 final byte[] bytes) throws IOException {
        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();
    }

    /**
     * Read a sequence of bytes from the given socket.
     * @param socket some socket
     * @param expectedBytes number of bytes to read
     * @return byte array contains `expectedBytes` bytes
     * @throws IOException
     */
    public static byte[] readBytes(final Socket socket,
                                   final Integer expectedBytes)
            throws IOException {
        byte[] buffer = new byte[expectedBytes];
        Integer actualBytes = socket.getInputStream()
                .read(buffer, 0, expectedBytes);
        if (!actualBytes.equals(expectedBytes)) {
            throw new IOException("read less bytes than expected");
        }
        return buffer;
    }

    /**
     * Send an integer (4 bytes) via the given socket.
     * @param socket some socket
     * @param integer the integer to send
     * @throws IOException
     */
    public static void sendInteger(final Socket socket,
                                   final Integer integer) throws IOException {
        sendBytes(socket, Ints.toByteArray(integer));
    }

    /**
     * Read an integer (4 bytes) from the given socket.
     * @param socket some socket
     * @return an integer read from the given socket
     * @throws IOException
     */
    public static Integer readInteger(final Socket socket) throws IOException {
        byte[] bytes = readBytes(socket, Integer.BYTES);
        return Ints.fromByteArray(bytes);
    }

    /**
     * Check if there are bytes to read.
     * @param socket some socket
     * @return <code>true</code> iff there is at least 1 byte to read
     * @throws IOException
     */
    public static Boolean availableToRead(final Socket socket)
            throws IOException {
        return socket.getInputStream().available() > 0;
    }
}
