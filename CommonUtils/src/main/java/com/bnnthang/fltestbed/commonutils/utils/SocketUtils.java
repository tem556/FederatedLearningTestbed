package com.bnnthang.fltestbed.commonutils.utils;

import com.bnnthang.fltestbed.commonutils.models.PowerConsumptionFromBytes;
import org.nd4j.shade.guava.primitives.Ints;

import java.io.IOException;
import java.io.OutputStream;
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
     * @throws IOException if I/O errors occur
     */
    public static void sendBytesWrapper(final Socket socket, final byte[] bytes) throws IOException {
        sendInteger(socket, bytes.length);
        sendBytes(socket, bytes);
    }

    public static void sendBytesWrapper(final Socket socket, final byte[] bytes, PowerConsumptionFromBytes power) throws IOException {
        sendBytesWrapper(socket, bytes);
        power.increasePowerConsumption((long) bytes.length);
    }

    /**
     * Enhanced wrapper for <code>readBytes</code>.
     * @param socket some socket
     * @return read buffer
     * @throws IOException if I/O errors occur
     */
    public static byte[] readBytesWrapper(final Socket socket) throws IOException {
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

    public static byte[] readBytesWrapper(final Socket socket, PowerConsumptionFromBytes power) throws IOException {
        byte[] bytes = readBytesWrapper(socket);
        power.increasePowerConsumption((long) bytes.length);
        return bytes;
    }

    /**
     * Read from a socket and directly write to another output stream.
     * @param socket some socket
     * @param outputStream some output stream
     * @return the number of bytes read
     * @throws IOException if I/O errors happen
     */
    public static Long readAndSaveBytes(final Socket socket, final OutputStream outputStream) throws IOException {
        // read the expected length
        int size = readInteger(socket);

        // initialize the buffer and counter
        byte[] buffer = new byte[BUFFER_SIZE];
        int current = 0;

        // read until reach the expected length
        while (current < size) {
            // read to buffer
            int readBytes = socket.getInputStream().read(buffer, 0, Integer.min(BUFFER_SIZE, size - current));

            // save to file
            outputStream.write(buffer);
            outputStream.flush();

            // increase the counter
            current += readBytes;
        }

        if (current > size) {
            throw new IOException(String.format("read: %d bytes, expected: %d bytes", current, size));
        }

        return (long) size;
    }

    public static Long readAndSaveBytes(final Socket socket, final OutputStream outputStream, PowerConsumptionFromBytes power) throws IOException {
        Long readBytes = readAndSaveBytes(socket, outputStream);
        power.increasePowerConsumption(readBytes);
        return readBytes;
    }

    /**
     * Send a sequence of bytes via the given socket.
     * @param socket some socket
     * @param bytes byte array to send
     * @throws IOException if I/O errors occur
     */
    public static void sendBytes(final Socket socket, final byte[] bytes) throws IOException {
        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();
    }

    public static void sendBytes(final Socket socket, final byte[] bytes, PowerConsumptionFromBytes power) throws IOException {
        sendBytes(socket, bytes);
        power.increasePowerConsumption((long) bytes.length);
    }

    /**
     * Read a sequence of bytes from the given socket.
     * @param socket some socket
     * @param expectedBytes number of bytes to read
     * @return byte array contains <code>expectedBytes</code> bytes
     * @throws IOException if I/O errors occur
     */
    public static byte[] readBytes(final Socket socket, final Integer expectedBytes) throws IOException {
        byte[] buffer = new byte[expectedBytes];
        Integer actualBytes = socket.getInputStream()
                .read(buffer, 0, expectedBytes);
        if (!actualBytes.equals(expectedBytes)) {
            throw new IOException("read less bytes than expected");
        }
        return buffer;
    }

    public static byte[] readBytes(final Socket socket, final Integer expectedBytes, PowerConsumptionFromBytes power) throws IOException {
        byte[] buffer = readBytes(socket, expectedBytes);
        power.increasePowerConsumption((long) expectedBytes);
        return buffer;
    }

    /**
     * Send an integer (4 bytes) via the given socket.
     * @param socket some socket
     * @param integer the integer to send
     * @throws IOException if I/O errors occur
     */
    public static void sendInteger(final Socket socket, final Integer integer) throws IOException {
        sendBytes(socket, Ints.toByteArray(integer));
    }

    public static void sendInteger(final Socket socket, final Integer integer, PowerConsumptionFromBytes power) throws IOException {
        sendInteger(socket, integer);
        power.increasePowerConsumption(4L);
    }

    /**
     * Read an integer (4 bytes) from the given socket.
     * @param socket some socket
     * @return an integer read from the given socket
     * @throws IOException if I/O errors occur
     */
    public static Integer readInteger(final Socket socket) throws IOException {
        byte[] bytes = readBytes(socket, Integer.BYTES);
        return Ints.fromByteArray(bytes);
    }

    public static Integer readInteger(final Socket socket, PowerConsumptionFromBytes power) throws IOException {
        Integer res = readInteger(socket);
        power.increasePowerConsumption(4L);
        return res;
    }

    /**
     * Check if there are bytes to read.
     * @param socket some socket
     * @return <code>true</code> iff there is at least 1 byte to read
     * @throws IOException if I/O errors occur
     */
    public static Boolean availableToRead(final Socket socket)
            throws IOException {
        return socket.getInputStream().available() > 0;
    }
}
