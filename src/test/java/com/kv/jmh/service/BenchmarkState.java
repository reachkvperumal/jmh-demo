package com.kv.jmh.service;

import com.kv.jmh.JmhDemoApplication;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openjdk.jmh.annotations.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.management.ManagementFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


@State(Scope.Benchmark)
public class BenchmarkState implements AutoCloseable {

    private static final AtomicBoolean TEAR_DOWN_FLAG = new AtomicBoolean(false);
    private static final CountDownLatch TEAR_DOWN_CTX = new CountDownLatch(3);

    private ConfigurableApplicationContext context;

    public ConfigurableApplicationContext getContext() {
        return context;
    }

    @Setup(Level.Trial)
    public void init() {
        System.out.printf("Initializing Spring context in fork: %s%n", System.identityHashCode(this));
        System.out.printf("Setup executed in JVM: %s%n", ManagementFactory.getRuntimeMXBean().getName());
        context = SpringApplication.run(JmhDemoApplication.class);
        /*context = new SpringApplicationBuilder(JmhDemoApplication.class)
                .properties("server.port=0")
                .run();*/
    }

    @TearDown(Level.Trial)
    public void shutdown() {
        if (TEAR_DOWN_FLAG.compareAndSet(false, true)) {
            try {
                System.out.printf("Setup executed in JVM: %s%n", ManagementFactory.getRuntimeMXBean().getName());
            } finally {
                TEAR_DOWN_CTX.countDown();
                context.close();
                System.out.printf("Setup executed in JVM: %s%n", ManagementFactory.getRuntimeMXBean().getName());
            }
        }

    }

    @Override
    public void close() {
        shutdown();
        try {
            TEAR_DOWN_CTX.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
