package com.kv.jmh.util;

public class ArrayExample {

    private static final int ARRAY_SIZE = 1024;
    private final long[] array = new long[ARRAY_SIZE];

    public void updateArray(int index) {
        array[index]++;
    }

    public static void main(String[] args) {
        ArrayExample example = new ArrayExample();

        // Threads updating different parts of the array
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < ARRAY_SIZE / 2; i++) {
                example.updateArray(i);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = ARRAY_SIZE / 2; i < ARRAY_SIZE; i++) {
                example.updateArray(i);
            }
        });

        t1.start();
        t2.start();
    }
}

