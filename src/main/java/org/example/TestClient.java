package org.example;

import org.example.packets.PacketRegistery;
import org.example.packets.TestPacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SocketChannel;

public class TestClient {

    private PacketRegistery registery;

    public static void main(String[] args) {
        new TestClient();
    }

    public TestClient() {

        this.registery = new PacketRegistery();
        try {
            runClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void runClient() throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(2020));
        while (true) {
            TestPacket packet = new TestPacket(System.nanoTime());
            registery.sendPacket(channel, packet);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }



    }

}
