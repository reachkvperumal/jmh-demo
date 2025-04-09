package com.kv.jmh.util.metrics;

import com.kv.jmh.service.InfoServiceBenchmarkTest;
import com.kv.jmh.util.dsa.ExecuteWithList;
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
public class RunWithListTest {
    @Benchmark
    @Fork(3)
    @Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.MILLISECONDS)
 //   @Timeout(time = 2, timeUnit = TimeUnit.SECONDS)
    public void trace(Blackhole blackhole) {
        ExecuteWithList executeWithList = new ExecuteWithList();
        blackhole.consume(executeWithList.compute());
    }

    public static void main(String[] args) throws Exception {
        // Set the console encoding to UTF-8
      /*  System.setProperty("file.encoding", "UTF-8");
        Charset charset = Charset.forName("UTF-8");
        System.setOut(new PrintStream(System.out, true, charset));
        System.setErr(new PrintStream(System.err, true, charset));*/

        Options options = new OptionsBuilder()
                .include(RunWithListTest.class.getSimpleName()) // Specify the benchmark class
                .addProfiler(GCProfiler.class) // Monitor the Garbage Collector
                .exclude(RunWithOutPadTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(RunWithArrayTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(RunWithPadTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(InfoServiceBenchmarkTest.class.getSimpleName()) // Exclude other benchmarks
                .output("result/worst-case-results.txt")
                .jvmArgs("-Dfile.encoding=UTF-8")
                .jvmArgs("-Djava.rmi.server.hostname=localhost")
                .build();

        new Runner(options).run();
    }
}
