package org.example;

import org.example.packets.FischPacket;
import org.example.packets.PacketRegistery;
import org.example.packets.TestPacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

public class TestClient {

    private PacketRegistery registery;
    private Worker worker;

    public static void main(String[] args) {
        new TestClient();
    }

    public TestClient() {

        this.registery = new PacketRegistery();
        try {
            this.worker = new Worker(registery);
            Executors.newFixedThreadPool(1).execute(this.worker);
            runClient();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void runClient() throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(2020));
        channel.configureBlocking(false);
        this.worker.registerChannel(channel);

        while(true) {
            TestPacket packet = new TestPacket(System.nanoTime());
            registery.sendPacket(channel, packet);

            registery.sendPacket(channel, new FischPacket("Hallo das ist ein Test"));

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }



    }

}
