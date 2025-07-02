package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TestServer {

    private List<Worker> workerList = new ArrayList<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        new TestServer();
    }

    public TestServer() {
        runServer(2);
    }

    public void runServer(int workers) {
        try {
            for (int i = 0; i < workers; i++) {
                Worker worker = new Worker();
                workerList.add(worker);
                executorService.submit(worker);
            }
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(2020));

            System.out.println("Started normal server");

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            Thread acceptorThread = new Thread(() -> {
                int workerIndex = 0;

                while (true) {
                    try {
                        int ready = selector.select();
                        if (ready == 0) continue;
                        for (SelectionKey selectedKey : selector.selectedKeys()) {
                            if (selectedKey.isAcceptable()) {
                                SocketChannel channel = serverSocketChannel.accept();
                                channel.configureBlocking(false);
                                System.out.println("Client connected " + channel.getRemoteAddress().toString() + " [WorkerID: "+workerIndex+"]");

                                workerList.get(workerIndex).registerChannel(channel);
                                workerIndex = workerIndex == (workers-1) ? 0 : workerIndex+1;
                            }
                        }
                        selector.selectedKeys().clear();
                    }catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            acceptorThread.setDaemon(true);
            acceptorThread.setName("Acceptor-Thread");
            acceptorThread.start();

            while (true) {

            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
