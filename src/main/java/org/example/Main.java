package org.example;

public class Main {

    private final DirectBufferPool pool;
    private final long startup;
    public Main() {
        startup = System.nanoTime();
        this.pool = new DirectBufferPool();
        for(int i = 0; i < 100; i++) {
            DirectBuffer byteBuffer = pool.buffer();
            byteBuffer.writeString("Das ist eine Message");
            byteBuffer.writeString("Das ist eine Message");
            byteBuffer.writeString("Das ist eine Message");
            byteBuffer.writeString("Das ist eine Message");

            pool.release(byteBuffer);
        }

        double needed = System.nanoTime() - startup;
        System.out.println("Needed: "+(needed/1000000)+"ms");
    }

    public static void main(String[] args) {
        new Main();
    }
}
