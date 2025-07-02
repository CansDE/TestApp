package org.example;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DirectBufferPool {

    private final Deque<DirectBuffer> pool;
    private final int MIN_CAPACITY = 256;
    private int MAX_POOL_SIZE = Integer.MAX_VALUE;

    public DirectBufferPool() {
        this.pool = new ConcurrentLinkedDeque<>();
    }

    public DirectBufferPool(int maxPoolSize) {
        this.MAX_POOL_SIZE = maxPoolSize;
        this.pool = new ConcurrentLinkedDeque<>();
    }

    public DirectBuffer buffer() {
        DirectBuffer byteBuffer = pool.poll();
        if(byteBuffer == null) {
            byteBuffer = new DirectBuffer(MIN_CAPACITY);
        }
        return byteBuffer;
    }

    public void release(DirectBuffer byteBuffer) {
        if (size() < MAX_POOL_SIZE) {
            if(byteBuffer.size() <= MIN_CAPACITY) {
                byteBuffer.clear();
                pool.push(byteBuffer);
                return;
            }
            return;
        }
        byteBuffer = null;
    }

    public int size() {
        return pool.size();
    }
}
