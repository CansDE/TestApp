package org.example;

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

    public Worker() {
        try {
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

                            int buff = channel.read(directBuffer.getRawBuffer());
                            if (buff == -1) {
                                System.out.println("Client disconnected");
                                channel.close();
                                return;
                            }
                            if(buff > 0) {
                                directBuffer.writerIndex += buff;
                            }
                            byte id = directBuffer.readByte();
                            int length = directBuffer.readInt();
                            long nanos = (System.nanoTime() - directBuffer.readLong());
                            System.out.println(id +":"+length+":" + nanos +"ns");
                            directBuffer.clear();
                        }catch (IOException ex) {
                            channel.close();
                            System.out.println("Client disconnected");
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
