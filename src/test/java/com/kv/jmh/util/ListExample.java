package com.kv.jmh.util;

import java.util.ArrayList;
import java.util.List;

public class ListExample {

    private static final int LIST_SIZE = 1024;
    private final List<Long> list = new ArrayList<>(LIST_SIZE);

    public ListExample() {
        for (int i = 0; i < LIST_SIZE; i++) {
            list.add(0L);
        }
    }

    public void updateList(int index) {
        list.set(index, list.get(index) + 1);
    }

    public static void main(String[] args) {
        ListExample example = new ListExample();

        // Threads updating different parts of the list
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < LIST_SIZE / 2; i++) {
                example.updateList(i);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = LIST_SIZE / 2; i < LIST_SIZE; i++) {
                example.updateList(i);
            }
        });

        t1.start();
        t2.start();
    }
}

