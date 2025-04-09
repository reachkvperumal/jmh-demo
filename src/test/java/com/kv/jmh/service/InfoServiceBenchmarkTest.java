package com.kv.jmh.service;

import com.kv.jmh.util.metrics.RunWithArrayTest;
import com.kv.jmh.util.metrics.RunWithListTest;
import com.kv.jmh.util.metrics.RunWithOutPadTest;
import com.kv.jmh.util.metrics.RunWithPadTest;
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
public class InfoServiceBenchmarkTest {

    @Benchmark
    @Fork(3)
    @Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.MILLISECONDS)
   // @Timeout(time = 2, timeUnit = TimeUnit.SECONDS)
    public void getGadgetInfoTest(Blackhole blackhole, BenchmarkState state) {
        InfoService service = state.getContext().getBean(InfoService.class);
        blackhole.consume(service.getGadgetInfo());
    }


    public static void main(String[] args) throws Exception {
        //Main.main(args);
        Options opt = new OptionsBuilder()
                .include(InfoServiceBenchmarkTest.class.getSimpleName()) // Specify the benchmark class
                .addProfiler(GCProfiler.class) // Monitor the Garbage Collector
                .exclude(RunWithOutPadTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(RunWithArrayTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(RunWithListTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(RunWithPadTest.class.getSimpleName()) // Exclude other benchmarks
                .output("result/benchmark-results.txt")
                .jvmArgs("-Dfile.encoding=UTF-8")
                .jvmArgs("-Djava.rmi.server.hostname=localhost")
                .build();

        new Runner(opt).run();
    }
}
