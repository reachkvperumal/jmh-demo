package com.kv.jmh.service;

import com.kv.jmh.util.metrics.BestCaseTest;
import com.kv.jmh.util.metrics.WorstCaseTest;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
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
    @Timeout(time = 2, timeUnit = TimeUnit.SECONDS)
    public void getGadgetInfoTest(Blackhole blackhole, BenchmarkState state) {
        InfoService service = state.getContext().getBean(InfoService.class);
        blackhole.consume(service.getGadgetInfo());
    }


    public static void main(String[] args) throws Exception {
        //Main.main(args);
        Options opt = new OptionsBuilder()
                .include(InfoServiceBenchmarkTest.class.getSimpleName())
                .exclude(BestCaseTest.class.getSimpleName())
                .exclude(WorstCaseTest.class.getSimpleName())
                .output("benchmark-results.txt")
                .jvmArgs("-Dfile.encoding=UTF-8")
                .build();

       // new Runner(opt).run();
    }
}
