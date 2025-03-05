package com.kv.jmh.util.dsa.falsesharing;

import com.kv.jmh.util.dsa.model.NonPaddedAtomicCounter;

public class WithOutPadding {
    static class NonPaddedThreads extends Thread {

        private final NonPaddedAtomicCounter counter;
        private final int iterationCount;

        public NonPaddedThreads(NonPaddedAtomicCounter counter, int iterationCount) {
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
        NonPaddedAtomicCounter counter = new NonPaddedAtomicCounter();
        NonPaddedThreads[] nonPaddedThreads = new NonPaddedThreads[threadCount];

        for (int i = 0; i < threadCount; i++) {
            nonPaddedThreads[i] = new NonPaddedThreads(counter, iterationCount);
            nonPaddedThreads[i].start();
        }

        for (NonPaddedThreads thread : nonPaddedThreads) {
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
