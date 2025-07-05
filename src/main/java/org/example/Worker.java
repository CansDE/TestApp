package org.example;

import org.example.packets.PacketRegistery;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Worker implements Runnable {

    private Selector selector;

    private DirectBuffer directBuffer = new DirectBuffer(256);
    private PacketRegistery packetRegistery;
    private int id;

    public Worker(int id, PacketRegistery packetRegistery) {
        try {
            this.id = id;
            this.packetRegistery = packetRegistery;
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                int ready = selector.select(10);
                if (ready == 0) continue;

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while(iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    SocketChannel channel = (SocketChannel) key.channel();
                    if (key.isReadable()) {
                        handleRead(channel);
                    }
                    /*
                    if(key.isWritable()) {
                        handleWrite(channel);
                    }

                     */
                }

            }catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    private void handleRead(SocketChannel channel) throws IOException {

        try {

            int read = directBuffer.read(channel);
            if (read == -1) {
                System.out.println("Client disconnected (Read = -1)");
                channel.close();
                return;
            }
            packetRegistery.callPacket(channel, directBuffer);
            directBuffer.clear();
        }catch (IOException ex) {
            channel.close();
            System.out.println("Client disconnected (IOException)");
        }
    }

    private void handleWrite(SocketChannel channel) {

    }

    public void registerChannel(SocketChannel channel) {
        try {
            channel.register(selector, SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }
    }

    public int id() {
        return id;
    }
}
