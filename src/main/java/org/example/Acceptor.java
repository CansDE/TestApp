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

        Thread acceptorThread = new Thread(() -> {
            int workerIndex = 0;

            while (true) {
                try {
                    int ready = selector.select();
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
        });
        acceptorThread.setDaemon(true);
        acceptorThread.setName("Acceptor-Thread");
        acceptorThread.start();
    }

    private void handleAccept(SocketChannel channel) throws IOException {
        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        System.out.println("Client connected " + channel.getRemoteAddress().toString());

        Worker worker = workerPool.nextWorker();
        System.out.println("worker: " + worker.id());
        worker.registerChannel(channel);
    }
}
