package com.bnnthang.fltestbed.commonutils.utils;

import com.bnnthang.fltestbed.commonutils.clients.IClientNetworkStatManager;
import com.bnnthang.fltestbed.commonutils.models.TimedValue;

import org.nd4j.shade.guava.primitives.Ints;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

public final class SocketUtils {
    /**
     * Empty constructor.
     */
    private SocketUtils() { }

    /**
     * Maximum buffer size.
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * Enhanced wrapper for <code>sendBytes</code>.
     * @param socket some socket.
     * @param bytes byte array to send.
     * @return the elapsed time.
     * @throws IOException if I/O errors occur.
     */
    public static TimedValue<Long> sendBytesWrapper(final Socket socket, final byte[] bytes) throws IOException {
        TimedValue<Long> foo1 = sendInteger(socket, bytes.length);
        TimedValue<Long> foo2 = sendBytes(socket, bytes);
        return new TimedValue<Long>(foo1.getValue() + foo2.getValue(), foo1.getElapsedTime() + foo2.getElapsedTime());
    }

    /**
     * Enhanced wrapper for <code>readBytes</code>.
     * @param socket some socket
     * @return read buffer and elapsed time.
     * @throws IOException if I/O errors occur
     */
    public static TimedValue<byte[]> readBytesWrapper(final Socket socket) throws IOException {
        TimedValue<Integer> foo = readInteger(socket);
        int size = foo.getValue();
        byte[] bytes = new byte[size];
        byte[] buffer = new byte[BUFFER_SIZE];
        int current = 0;
        double totalTime = foo.getElapsedTime();
        while (current < size) {
            // read to buffer
            LocalDateTime t0 = LocalDateTime.now();
            int readBytes = socket.getInputStream().read(buffer, 0, Integer.min(BUFFER_SIZE, size - current));
            LocalDateTime t1 = LocalDateTime.now();

            // copy to result
            System.arraycopy(buffer, 0, bytes, current, readBytes);

            // increase counter
            current += readBytes;
            totalTime += TimeUtils.millisecondsBetween(t0, t1);
        }
        return new TimedValue<byte[]>(bytes, totalTime);
    }

    /**
     * Read from a socket and directly write to another output stream.
     * @param socket some socket
     * @param outputStream some output stream
     * @return the number of bytes read and the elapsed time.
     * @throws IOException if I/O errors happen
     */
    public static TimedValue<Long> readAndSaveBytes(final Socket socket, final OutputStream outputStream) throws IOException {
        // read the expected length
        TimedValue<Integer> foo = readInteger(socket);
        int size = foo.getValue();
        int current = 0;
        Double totalTime = foo.getElapsedTime();
        while (current < size) {
            int toRead = Integer.min(BUFFER_SIZE, size - current);
            byte[] buf = new byte[toRead];
            int bytesRead = socket.getInputStream().read(buf);

            LocalDateTime t0 = LocalDateTime.now();
            outputStream.write(buf, 0, bytesRead);
            outputStream.flush();
            LocalDateTime t1 = LocalDateTime.now();

            totalTime += TimeUtils.millisecondsBetween(t0, t1);
            current += bytesRead;
        }

        return new TimedValue<Long>((long) size, totalTime);
    }

    /**
     * Send a sequence of bytes via the given socket.
     * @param socket some socket
     * @param bytes byte array to send
     * @return the number of bytes sent and the elapsed time.
     * @throws IOException if I/O errors occur
     */
    public static TimedValue<Long> sendBytes(final Socket socket, final byte[] bytes) throws IOException {
        LocalDateTime t0 = LocalDateTime.now();
        socket.getOutputStream().write(bytes);
        socket.getOutputStream().flush();
        LocalDateTime t1 = LocalDateTime.now();
        return new TimedValue<Long>((long) bytes.length, TimeUtils.millisecondsBetween(t0, t1));
    }

    /**
     * Read a sequence of bytes from the given socket.
     * @param socket some socket.
     * @param expectedBytes number of bytes to read.
     * @return byte array contains <code>expectedBytes</code> bytes and elapsed time.
     * @throws IOException if I/O errors occur
     */
    public static TimedValue<byte[]> readBytes(final Socket socket, final Integer expectedBytes) throws IOException {
        byte[] buffer = new byte[expectedBytes];
        LocalDateTime t0 = LocalDateTime.now();
        Integer actualBytes = socket.getInputStream().read(buffer, 0, expectedBytes);
        if (!actualBytes.equals(expectedBytes)) {
            throw new IOException("read less bytes than expected");
        }
        LocalDateTime t1 = LocalDateTime.now();
        return new TimedValue<byte[]>(buffer, TimeUtils.millisecondsBetween(t0, t1));
    }

    /**
     * Send an integer (4 bytes) via the given socket.
     * @param socket some socket.
     * @param integer integer to send.
     * @return the number of bytes sent and the elasped time.
     * @throws IOException if I/O errors occur.
     */
    public static TimedValue<Long> sendInteger(final Socket socket, final Integer integer) throws IOException {
        return sendBytes(socket, Ints.toByteArray(integer));
    }

    /**
     * Read an integer (4 bytes) from the given socket.
     * @param socket some socket.
     * @return an integer read from the given socket and the elapsed time.
     * @throws IOException if I/O errors occur.
     */
    public static TimedValue<Integer> readInteger(final Socket socket) throws IOException {
        TimedValue<byte[]> foo = readBytes(socket, Integer.BYTES);
        return new TimedValue<Integer>(Ints.fromByteArray(foo.getValue()), foo.getElapsedTime());
    }

    /**
     * Check if there are bytes to read.
     * @param socket some socket.
     * @return <code>true</code> iff there is at least 1 byte to read.
     * @throws IOException if I/O errors occur.
     */
    public static Boolean availableToRead(final Socket socket) throws IOException {
        return socket.getInputStream().available() > 0;
    }

    public static void clientTrackedSendBytesWrapper(final Socket socket, final byte[] bytes, final IClientNetworkStatManager statManager) throws IOException {
        // send bytes
        TimedValue<Long> foo = sendBytesWrapper(socket, bytes);

        // update stats
        statManager.increaseBytes(foo.getValue());
        statManager.increaseCommTime(foo.getElapsedTime());
    }

    public static byte[] clientTrackedReadBytesWrapper(final Socket socket, final IClientNetworkStatManager statManager) throws IOException {
        // read bytes
        TimedValue<byte[]> foo = readBytesWrapper(socket);

        // update stats
        statManager.increaseBytes((long) foo.getValue().length);
        statManager.increaseCommTime(foo.getElapsedTime());

        return foo.getValue();
    }

    public static void clientTrackedSendInteger(final Socket socket, final Integer integer, final IClientNetworkStatManager statManager) throws IOException {
        // send integer
        TimedValue<Long> foo = sendInteger(socket, integer);

        // update stats
        statManager.increaseBytes(foo.getValue());
        statManager.increaseCommTime(foo.getElapsedTime());
    }

    public static Integer clientTrackedReadInteger(final Socket socket, final IClientNetworkStatManager statManager) throws IOException {
        // read integer
        TimedValue<Integer> foo = readInteger(socket);

        // update stats
        statManager.increaseBytes(4L);
        statManager.increaseCommTime(foo.getElapsedTime());

        return foo.getValue();
    }

    public static void serializeAndSend(final Socket socket, Object obj) throws IOException {
        // serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(obj);
        out.flush();
        byte[] bytes = bos.toByteArray();

        // send
        SocketUtils.sendBytes(socket, bytes);

        // clean
        bos.close();
        out.close();
    }
}
