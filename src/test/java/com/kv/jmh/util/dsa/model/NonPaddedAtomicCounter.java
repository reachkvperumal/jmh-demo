package com.kv.jmh.util.dsa.model;

import java.util.concurrent.atomic.AtomicLong;

public class NonPaddedAtomicCounter {
    private final AtomicLong atomicCounter = new AtomicLong(0);

    public void incrementCounterValue() {
        atomicCounter.incrementAndGet();
    }

    public long getCounterValue(){
        return atomicCounter.get();
    }
}
