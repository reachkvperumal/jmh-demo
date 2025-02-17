package com.kv.jmh.util.metrics;

import com.kv.jmh.service.InfoServiceBenchmarkTest;
import com.kv.jmh.util.BestCaseScenario;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
public class WorstCaseTest {
    @Benchmark
    @Fork(3)
    @Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.MILLISECONDS)
    @Timeout(time = 2, timeUnit = TimeUnit.SECONDS)
    public void trace(Blackhole blackhole){
        BestCaseScenario bestCaseScenario = new BestCaseScenario();
        blackhole.consume(bestCaseScenario.compute());
    }

    public static void main(String[] args) throws Exception {
        // Set the console encoding to UTF-8
      /*  System.setProperty("file.encoding", "UTF-8");
        Charset charset = Charset.forName("UTF-8");
        System.setOut(new PrintStream(System.out, true, charset));
        System.setErr(new PrintStream(System.err, true, charset));*/

        Options options = new OptionsBuilder()
                .include(WorstCaseTest.class.getSimpleName())
                .exclude(BestCaseTest.class.getSimpleName())// Specify the benchmark class
                .exclude(InfoServiceBenchmarkTest.class.getSimpleName())
                .output("worst-case-results.txt")
                .jvmArgs("-Dfile.encoding=UTF-8")// Exclude other benchmarks
                .build();

        new Runner(options).run();
    }
}
