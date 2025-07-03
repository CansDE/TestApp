package org.example;

import org.example.packets.FischPacket;
import org.example.packets.PacketRegistery;
import org.example.packets.TestPacket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOptions;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class TestServer {

    private PacketRegistery packetRegistery;
    private Acceptor acceptor;
    private WorkerPool workerPool;
    private ExecutorService service = Executors.newSingleThreadExecutor();

    public static void main(String[] args) {
        new TestServer();
    }

    public TestServer() {
        this.packetRegistery = new PacketRegistery();
        runServer(2);
    }

    public void runServer(int workers) {
        try {

            packetRegistery.handle(FischPacket.class, (socketChannel, fischPacket) -> {
                try {
                    System.out.println(socketChannel.getLocalAddress().toString() + ":"+fischPacket.getString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            packetRegistery.handle(TestPacket.class, (socketChannel, packet) -> {
                try {
                    System.out.println(socketChannel.getRemoteAddress().toString() + ":" + (System.nanoTime() - packet.getTime()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            this.workerPool = new WorkerPool(workers, packetRegistery);
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(2020));

            System.out.println("Started normal server");

            this.acceptor = new Acceptor(serverSocketChannel, workerPool);
            service.submit(this.acceptor);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
