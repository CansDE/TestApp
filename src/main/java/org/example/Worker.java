package org.example;

import org.example.packets.PacketRegistery;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Worker implements Runnable {

    private Selector selector;
    private PacketRegistery packetRegistery;
    private int id;

    private Queue<SocketChannel> pendingConnections = new ConcurrentLinkedQueue<>();

    /*
    TEMP::::
     */
    public Connection connection;
    public boolean connected = false;

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
                int ready = selector.select();

                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                while(iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    Connection connection = (Connection) key.attachment();
                    if (key.isReadable()) {
                        connection.doRead();
                    }

                    if(key.isWritable()) {
                        connection.doWrite();
                    }
                }
                registerPendingConnections();

            }catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }


    public void registerPendingConnections() {
        while(!pendingConnections.isEmpty()) {
            SocketChannel channel = pendingConnections.poll();
            try {
                channel.configureBlocking(false);
                channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
                SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
                Connection connection = new Connection(channel, key, packetRegistery);
                key.attach(connection);

                this.connection = connection;
                this.connected = true;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void registerChannel(SocketChannel channel) {
        pendingConnections.add(channel);
        selector.wakeup();
    }

    public int id() {
        return id;
    }
}
