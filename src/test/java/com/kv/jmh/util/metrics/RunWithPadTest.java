package com.kv.jmh.util.metrics;

import com.kv.jmh.service.InfoServiceBenchmarkTest;
import com.kv.jmh.util.dsa.falsesharing.WithPadding;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
public class RunWithPadTest {

    @Benchmark
    @Fork(3)
    @Measurement(iterations = 3, time = 10, timeUnit = TimeUnit.SECONDS)
  //  @Timeout(time = 5, timeUnit = TimeUnit.SECONDS)
    public void trace(Blackhole blackhole){
        WithPadding wp = new WithPadding();
        blackhole.consume(wp.compute(4,500_000));
    }

    public static void main(String[] args) throws Exception {
        Options options = new OptionsBuilder()
                .include(RunWithPadTest.class.getSimpleName()) // Specify the benchmark class
                .addProfiler(GCProfiler.class) // Monitor the Garbage Collector
                .exclude(RunWithOutPadTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(RunWithArrayTest.class.getSimpleName())  // Exclude other benchmarks
                .exclude(InfoServiceBenchmarkTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(RunWithListTest.class.getSimpleName()) // Exclude other benchmarks
                .output("result/pad-case-results.txt")
                .jvmArgs("-Dfile.encoding=UTF-8")
                .jvmArgs("-Djava.rmi.server.hostname=localhost")
                .build();
        new Runner(options).run();
    }
}
