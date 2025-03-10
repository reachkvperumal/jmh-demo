package com.kv.jmh.util.dsa.falsesharing;

import com.kv.jmh.util.dsa.model.PaddedAtomicCounter;

public class WithPadding {

    static class PaddedThreads extends Thread {

        private final PaddedAtomicCounter counter;
        private final int iterationCount;

        public PaddedThreads(PaddedAtomicCounter counter, int iterationCount) {
            this.counter = counter;
            this.iterationCount = iterationCount;
        }

        @Override
        public void run() {
            for (int i = 0; i < iterationCount; i++) {
                counter.incrementCounterValue();
            }
        }
    }

    public long compute(int threadCount, int iterationCount) {
        PaddedAtomicCounter counter = new PaddedAtomicCounter();
        PaddedThreads[] paddedThreads = new PaddedThreads[threadCount];

        for (int i = 0; i < threadCount; i++) {
            paddedThreads[i] = new PaddedThreads(counter, iterationCount);
            paddedThreads[i].start();
        }

        for (PaddedThreads thread : paddedThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        long counterValue = counter.getCounterValue();
        System.out.printf("Actual Count: %d - Expected Count: %d%n ", counterValue, threadCount * iterationCount);
        return counterValue;
    }
}
