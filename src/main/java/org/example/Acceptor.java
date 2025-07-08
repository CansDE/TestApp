package org.example;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Acceptor implements Runnable{

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private WorkerPool workerPool;
    private int workerIndex = 0;

    public Acceptor(ServerSocketChannel serverSocketChannel, WorkerPool workerPool) {
        try {
            this.workerPool = workerPool;
            this.serverSocketChannel = serverSocketChannel;
            this.selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

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

                        if (key.isAcceptable()) {
                            SocketChannel channel = serverSocketChannel.accept();
                            handleAccept(channel);
                        }
                    }
                }catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
    }

    private void handleAccept(SocketChannel channel) throws IOException {
        System.out.println("Client connected " + channel.getRemoteAddress().toString());

        Worker worker = workerPool.nextWorker();
        worker.registerChannel(channel);
    }
}
