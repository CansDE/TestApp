package org.example;

import org.example.packets.PacketRegistery;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerPool {

    private List<Worker> workerList = new ArrayList<>();
    private ExecutorService executorService;
    private int workerIndex;
    private int poolSize;

    public WorkerPool(int poolSize, PacketRegistery packetRegistery) {
        this.poolSize = poolSize;
        this.executorService = Executors.newFixedThreadPool(poolSize);
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker(i, packetRegistery);
            workerList.add(worker);
            executorService.submit(worker);
        }
    }

    public Worker nextWorker() {
        workerIndex = workerIndex == poolSize ? 0 : workerIndex;

        return workerList.get(workerIndex++);
    }

}
