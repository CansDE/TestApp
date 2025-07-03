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

    public Worker(PacketRegistery packetRegistery) {
        try {
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
                int ready = selector.selectNow();
                if (ready == 0) continue;

                for (SelectionKey selectedKey : selector.selectedKeys()) {
                    if (selectedKey.isReadable()) {
                        SocketChannel channel = (SocketChannel) selectedKey.channel();
                        try {

                            int buff = directBuffer.read(channel);
                            if (buff == -1) {
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
                }

                selector.selectedKeys().clear();
            }catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public void registerChannel(SocketChannel channel) {
        try {
            channel.register(selector, SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            throw new RuntimeException(e);
        }
    }
}
