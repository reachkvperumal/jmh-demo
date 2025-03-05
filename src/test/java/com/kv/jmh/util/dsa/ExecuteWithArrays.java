package com.kv.jmh.util.dsa;

import java.util.concurrent.atomic.AtomicLongArray;

public class ExecuteWithArrays {

    private static final int ARRAY_SIZE = 1024;
    private final AtomicLongArray array = new AtomicLongArray(ARRAY_SIZE);

    private void updateArray(int index) {
        array.incrementAndGet(index);
    }

    public int compute(){
        // Threads updating different parts of the array
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < ARRAY_SIZE / 2; i++) {
                this.updateArray(i);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = ARRAY_SIZE / 2; i < ARRAY_SIZE; i++) {
                this.updateArray(i);
            }
        });

        t1.start();
        t2.start();
        return ARRAY_SIZE;
    }
}

