package com.kv.jmh.util.dsa.model;

import java.util.concurrent.atomic.AtomicLong;

public class PaddedAtomicCounter {
    private final AtomicLong atomicCounter = new AtomicLong(0);

    private long p1, p2, p3, p4, p5, p6, p7;

    public void incrementCounterValue() {
        atomicCounter.incrementAndGet();
    }

    public long getCounterValue(){
        return atomicCounter.get();
    }
}
