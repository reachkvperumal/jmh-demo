package com.kv.jmh.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class WorstCaseScenario {

    private static final int LIST_SIZE = 1024;
    private final List<AtomicLong> list = new ArrayList<>(LIST_SIZE);

    public WorstCaseScenario() {
        for (int i = 0; i < LIST_SIZE; i++) {
            list.add(new AtomicLong(0L));
        }
    }

    private void updateList(int index) {
        list.get(index).incrementAndGet(); // Atomic increment
    }

    public void compute(){
        // Threads updating different parts of the list
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < LIST_SIZE / 2; i++) {
                this.updateList(i);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = LIST_SIZE / 2; i < LIST_SIZE; i++) {
                this.updateList(i);
            }
        });

        t1.start();
        t2.start();
    }
}

