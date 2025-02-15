# jmh-demo
Use JMH to measure metrics

*Understanding the following concept makes JMH API much easier.*

## Field Reordering in Java
1. ### In Java, the JVM is allowed to reorder fields within a class to optimize memory layout and access patterns. 
   - For example, the JVM might group fields of the same type together to improve cache locality or alignment. 
   - However, this reordering is typically done within the same class, not across class hierarchies.
     Why Superclass Fields Are Not Reordered with Subclass Fields
     The statement explains that superclass fields are not reordered with subclass fields in practice. Here’s why:

2. ### Superclass and Subclass Fields
   - In a class hierarchy, a subclass inherits fields from its superclass. For example:
   ```java
class Superclass {
int a;
int b;
}

class Subclass extends Superclass {
int c;
int d;
}
```
  - Here, Subclass inherits fields a and b from Superclass and adds its own fields c and d.
```
3. ### Why Superclass Fields Are Not Reordered with Subclass Fields
   - The statement explains that superclass fields are not reordered with subclass fields in practice. Here’s why:
####  Polymorphic Access Overhead
   - If the JVM were to reorder superclass fields differently in different subclasses, accessing those fields polymorphically (i.e., through a superclass reference) would become inefficient.
   - For example, consider the following code:
``` java
Superclass obj = new Subclass();
obj.a = 10; // Accessing superclass field
```
   - If `a` were at different offsets in different subclasses, the JVM would need to determine the actual subclass type at runtime to access the correct memory location for `a`. 
   - This would introduce significant overhead for field access.
4. ### Memory Layout in Practice
   - In practice, the memory layout for a class hierarchy typically looks like this:
   - Superclass fields are stored first, in the order they are declared.
   - Subclass fields are stored after the superclass fields, in the order they are declared.
   - For example, the memory layout for Subclass might look like this:

|                    |  
|--------------------|
| Superclass field a |
| Superclass field b |
| Subclass field c   |
| Subclass field d   |

### This layout ensures that:

- Superclass fields have consistent offsets across all subclasses.

- Subclass fields are appended after the superclass fields.

5. ### Why This Matters for Performance
### Consistent Offsets: 
- Keeping superclass fields at consistent offsets allows the JVM to generate efficient bytecode for field access. 
- For example, the JVM can use fixed offsets to access a and b in Superclass without needing to know the actual subclass type.

### Cache Locality: 
- Grouping fields of the same class together improves cache locality, as fields that are often accessed together are stored close to each other in memory.

6. ### Example of Polymorphic Field Access
   Consider the following example:
```java
class Superclass {
    int a;
    int b;
}

class Subclass1 extends Superclass {
    int c;
}

class Subclass2 extends Superclass {
    int d;
}

Superclass obj1 = new Subclass1();
Superclass obj2 = new Subclass2();

obj1.a = 10; // Accessing superclass field
obj2.a = 20; // Accessing superclass field
```
__In this case:__ 

The JVM knows that a is always at the same offset in memory, regardless of whether the object is an instance of Subclass1 or Subclass2.

If a were at different offsets in Subclass1 and Subclass2, the JVM would need to perform additional work to determine the correct offset at runtime, which would slow down field access.

7. ### Summary
- It would introduce significant overhead for polymorphic field access.
- It would complicate the memory model and degrade performance.
- No practical JVM implementation does this because it is unnecessary and inefficient.
- By keeping superclass fields at consistent offsets, the JVM ensures efficient and predictable field access, which is critical for performance in object-oriented programs.

[Dead-Code Elimination](#Dead-Code Elimination)

[False Sharing](#False Sharing)

[Write Wall](#Write Wall)


## Dead-Code Elimination
- The JVM is highly optimized and may eliminate (or "optimize away") code that it determines is unnecessary. This is called Dead Code Elimination (DCE).
- If a benchmark method produces a result that is not used, the JVM might skip the computation entirely, leading to inaccurate benchmark results.
__Solution:__
- Use Blackholes to consume the results of computations. This ensures that the JVM cannot optimize away the code.

## False Sharing
- False sharing occurs when multiple threads modify variables that reside on the same CPU cache line.
- This causes cache line invalidation and forces the CPU to reload the cache line, degrading performance.
- In benchmarks, if multiple threads read/write shared state, false sharing can artificially inflate or deflate performance measurements.
__Solution:__
- Isolate critical fields to avoid false sharing. Use padding or separate cache lines for shared state.
```java
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class FalseSharingExample {

    private long value1; // Shared state
    private long value2; // Shared state

    @Benchmark
    public void benchmarkFalseSharing() {
        // Simulate false sharing
        value1++;
        value2++;
    }

    @Benchmark
    public void benchmarkNoFalseSharing() {
        // Isolate state to avoid false sharing
        long localValue1 = value1;
        long localValue2 = value2;
        localValue1++;
        localValue2++;
        value1 = localValue1;
        value2 = localValue2;
    }
}
```
- In benchmarkFalseSharing, value1 and value2 might reside on the same cache line, causing false sharing.
- In benchmarkNoFalseSharing, the state is isolated in local variables, avoiding false sharing.
 
## Write Wall
__Writing to memory is more expensive than reading because it:__
- Disturbs CPU caches.
- Pollutes write buffers.
- Can cause premature hitting of the memory wall (the point at which memory bandwidth becomes a bottleneck).
- Excessive writes in benchmarks can distort results by introducing unnecessary overhead.

__Solution:__
- Minimize writes in benchmarks. Prefer reading from memory when possible, as reads are cache-friendly.
```java
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class WriteWallExample {

    private long value;

    @Benchmark
    public void benchmarkExcessiveWrites() {
        // Excessive writes
        for (int i = 0; i < 1000; i++) {
            value = i;
        }
    }

    @Benchmark
    public void benchmarkMinimalWrites() {
        // Minimize writes
        long localValue = value;
        for (int i = 0; i < 1000; i++) {
            localValue = i;
        }
        value = localValue;
    }
}
```

### Key Differences Between False Sharing and Write Wall

|Aspect |	False Sharing	|Write Wall|
|-------|-------------------|----------|
|Scope	|Multi-threaded |	Single-threaded or Multi-threaded|
|Cause	|Cache line contention between threads |	Excessive writes to memory|
|Primary |Bottleneck	Cache line invalidation |	Memory bandwidth saturation|
|Performance |Impact	Degrades performance in multi-threaded contexts due to cache line invalidation|	Degrades performance due to memory subsystem overload|
|Solution	|Padding, thread-local storage |	Minimize writes, batch writes|

__Here, value1 and value2 are likely on the same cache line, causing false sharing between t1 and t2.__
```java
class FalseSharing {
    volatile long value1;
    volatile long value2; // Likely on the same cache line as value1

    void run() {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                value1++;
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                value2++;
            }
        });

        t1.start();
        t2.start();
    }
}
```
__Here, the loop performs a large number of writes to the data array, potentially saturating the memory bandwidth.__
```java
class WriteWall {
    long[] data = new long[1_000_000];

    void run() {
        for (int i = 0; i < data.length; i++) {
            data[i] = i; // Excessive writes to memory
        }
    }
}
```

__Summary of my Configuration__

|Annotation/Parameter|	Value/Setting| 	Purpose                                                              |
|--------------------|---------------|-----------------------------------------------------------------------|
|@BenchmarkMode(Mode.All) |	Run in all benchmark modes| 	Measure throughput, average time, sample time, and single-shot time. |
|@OutputTimeUnit(TimeUnit.MILLISECONDS)|	Report results in milliseconds| 	Results are displayed in milliseconds (e.g., ms/op).                 |
|@Warmup(iterations = 2, time = 2, ...)|	2 warmup iterations, 2 ms each| 	Allow the JVM to reach a steady state before measurements.           |
|@Measurement(iterations = 3, time = 2, ...)|	3 measurement iterations, 2 ms each| 	Collect benchmark results over 3 iterations.                         |
|@Fork(2)|	2 separate JVM processes| 	Ensure isolation and reproducibility by running in fresh JVMs.       |

__Following are my results from the run__

| Benchmark                                                                                |                                            Mode | Cnt |  Score  | Error   |  Units|
|------------------------------------------------------------------------------------------|-------------------------------------------------|-----|---------|---------|-------|
| InfoServiceBenchmarkTest.getGadgetInfoTest |           thrpt |   9|   0.072| ± 0.009 | ops/ms |
| InfoServiceBenchmarkTest.getGadgetInfoTest |           avgt  |  9 | 14.113 | ± 2.579 | ms/op  |
| InfoServiceBenchmarkTest.getGadgetInfoTest |         sample  |  9 | 15.184 | ± 2.719 | ms/op  |
| InfoServiceBenchmarkTest.getGadgetInfoTest|:p0.00    sample  |    | 13.173 |         | ms/op  |
| InfoServiceBenchmarkTest.getGadgetInfoTest|:p0.50    sample  |    | 15.270 |         | ms/op  |
| InfoServiceBenchmarkTest.getGadgetInfoTest|:p0.90    sample  |    | 17.138 |         | ms/op  |
| InfoServiceBenchmarkTest.getGadgetInfoTest|:p0.95    sample  |    | 17.138 |         | ms/op  |
| InfoServiceBenchmarkTest.getGadgetInfoTest|:p0.99    sample  |    | 17.138 |         | ms/op  |
| InfoServiceBenchmarkTest.getGadgetInfoTest|:p0.999   sample  |    | 17.138 |         | ms/op  |
| InfoServiceBenchmarkTest.getGadgetInfoTest|:p0.9999  sample  |    | 17.138 |         | ms/op  |
| InfoServiceBenchmarkTest.getGadgetInfoTest|:p1.00    sample  |    | 17.138 |         | ms/op  |
| InfoServiceBenchmarkTest.getGadgetInfoTest|              ss  |  9 | 19.244 | ± 3.180 | ms/op  |


---
> - Benchmark: Identifies the method being measured.  
> - Mode: Specifies the type of measurement (e.g., throughput, average time).  
> - Cnt: Indicates the number of measurement iterations.  
> - Score: The primary result of the benchmark.  
> - Error: The margin of error or confidence interval for the score.  
> - Units: The unit of measurement for the score.
