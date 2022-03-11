package com.bnnthang.fltestbed.network;

import com.google.common.primitives.Ints;

import java.io.IOException;
import java.net.Socket;

public final class SocketUtils {
    private SocketUtils() { }

    /**
     * Send a sequence of bytes via the given socket.
     * @param socket some socket
     * @param bytes byte array to send
     * @throws IOException if problems happen when calling socket's methods
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
     * @throws IOException if problems happen when calling socket's methods
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
     * @throws IOException if problems happen when calling socket's methods
     */
    public static void sendInteger(final Socket socket,
                                   final Integer integer) throws IOException {
        sendBytes(socket, Ints.toByteArray(integer));
    }

    /**
     * Read an integer (4 bytes) from the given socket.
     * @param socket some socket
     * @return an integer read from the given socket
     * @throws IOException if problems happen when calling socket's methods
     */
    public static Integer readInteger(final Socket socket) throws IOException {
        byte[] bytes = readBytes(socket, Integer.BYTES);
        return Ints.fromByteArray(bytes);
    }
}
