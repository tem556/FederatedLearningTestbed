package com.bnnthang.fltestbed.network;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.nd4j.shade.guava.primitives.Ints;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

@RunWith(MockitoJUnitRunner.class)
public class SocketUtilsTest {
    @Mock
    private Socket socket;

    @Test
    public void whenSendInteger_thenIntegerIsSend() throws IOException {
        // Arrange
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Mockito.when(socket.getOutputStream()).thenReturn(os);

        // Act
        SocketUtils.sendInteger(socket, 987654);

        // Assert
        Assert.assertEquals(Ints.fromByteArray(os.toByteArray()), 987654);
    }

    @Test
    public void whenReadInteger_thenIntegerIsRead() throws IOException {
        // Arrange
        Mockito.when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(Ints.toByteArray(123)));

        // Act
        int num = SocketUtils.readInteger(socket);

        // Assert
        Assert.assertEquals(num, 123);
    }
}
