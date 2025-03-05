package com.kv.jmh.util.metrics;

import com.kv.jmh.service.InfoServiceBenchmarkTest;
import com.kv.jmh.util.dsa.ExecuteWithArrays;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 3, timeUnit = TimeUnit.SECONDS)
public class RunWithArrayTest {

    @Benchmark
    @Fork(3)
    @Measurement(iterations = 3, time = 3, timeUnit = TimeUnit.MILLISECONDS)
  //  @Timeout(time = 2, timeUnit = TimeUnit.SECONDS)
    public void trace(Blackhole blackhole){
        ExecuteWithArrays executeWithArrays = new ExecuteWithArrays();
        blackhole.consume(executeWithArrays.compute());
    }

    public static void main(String[] args) throws Exception {
        // Set the console encoding to UTF-8
        /*System.setProperty("file.encoding", "UTF-8");
        Charset charset = Charset.forName("UTF-8");
        System.setOut(new PrintStream(System.out, true, charset));
        System.setErr(new PrintStream(System.err, true, charset));*/

        Options options = new OptionsBuilder()
                .include(RunWithArrayTest.class.getSimpleName()) // Specify the benchmark class
                .exclude(RunWithOutPadTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(InfoServiceBenchmarkTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(RunWithListTest.class.getSimpleName()) // Exclude other benchmarks
                .exclude(RunWithPadTest.class.getSimpleName()) // Exclude other benchmarks
                .output("result/best-case-results.txt")
                .jvmArgs("-Dfile.encoding=UTF-8")
                .build();

        new Runner(options).run();
    }

}
