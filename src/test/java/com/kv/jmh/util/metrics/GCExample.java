package com.kv.jmh.util.metrics;

import com.kv.jmh.service.InfoServiceBenchmarkTest;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
public class GCExample {

    @Benchmark
    public void stringConcat(Blackhole blackhole) {
        String result = "";
        for (int i = 0; i < 100; i++) {
            result += "x"; // High allocation
        }
        blackhole.consume(result);
    }

    @Benchmark
    public void stringBuilder(Blackhole blackhole) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("x"); // Low allocation
        }
        blackhole.consume(sb.toString());
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(GCExample.class.getSimpleName()) // Specify the benchmark class
                .addProfiler(GCProfiler.class) // Monitor the Garbage Collector
                .exclude(RunWithArrayTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(RunWithOutPadTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(InfoServiceBenchmarkTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(RunWithListTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(RunWithPadTest.class.getSimpleName()) // Exclude other benchmarks
                .output("result/GCExample.txt")
                .jvmArgs("-Dfile.encoding=UTF-8")
                .jvmArgs("-Djava.rmi.server.hostname=localhost")
                .build();

        new Runner(options).run();

    }
}
