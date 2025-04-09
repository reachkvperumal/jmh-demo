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

---

- [Dead-Code Elimination](#dead-code-elimination)
- [False Sharing](#false-sharing)
- [Write Wall](#write-wall)


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
### Key Differences Between Scopes

| Scope | Instances Created | Shared Between | Thread Safety Required? | Typical Use Case |
|---------------|---------------------------|-------------------------|-------------------------|-----------------------------------|
| `Benchmark` | 1 per benchmark | All threads | Yes | Shared resources, global state |
| `Thread` | 1 per thread | No sharing | No | Independent thread-local state |
| `Group` | 1 per group | Threads in the group | Yes | Producer-consumer, group dynamics |

---

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
> 
> Link to [benchmark-results.txt](/result/best-case-results.txt)
>
> ---
> To compare the **heap size support** and the **generational separation approach** (young and old generations) of **G1GC**, **Generational ZGC**, and **Shenandoah GC**, I’ll break down each collector’s capabilities and architecture with respect to these aspects. This will provide a clear comparison of their heap size limits and whether their young and old generations are physically or logically separated.

---

### **1. G1GC (Garbage-First Garbage Collector)**

#### **Heap Size Support**
- **Maximum Heap Size**: G1GC supports heaps up to **64TB** on 64-bit systems with compressed object pointers (CompressedOops) enabled. Without CompressedOops, the theoretical limit is higher but constrained by practical memory availability and JVM overhead.
- **Practical Range**: Commonly used for heaps from a few hundred MB to tens of GB (e.g., 1GB to 32GB), though it scales well to larger heaps (e.g., 100GB+) with proper tuning.
- **Constraints**:
  - G1GC’s performance can degrade with very large heaps due to longer pause times for young and mixed collections, especially if not tuned properly (e.g., adjusting `-XX:G1HeapRegionSize` or `-XX:MaxGCPauseMillis`).
  - Region-based management adds metadata overhead, which grows with heap size but is manageable for most workloads.

#### **Generational Separation**
- **Young and Old Generations**: G1GC has **physical separation** of young and old generations.
- **Heap Structure**:
  - The heap is divided into fixed-size **regions** (typically 1MB to 32MB, configurable via `-XX:G1HeapRegionSize`).
  - **Young Generation**: Comprises **Eden** and **Survivor** regions, where new objects are allocated and short-lived objects are collected.
  - **Old Generation**: Consists of regions containing long-lived objects promoted from the young generation.
  - **Physical Separation**: Eden, Survivor, and old regions are distinct sets of heap regions, explicitly assigned to either the young or old generation at any given time. Regions can dynamically switch roles (e.g., from old to Eden after cleanup), but at any moment, a region belongs to one generation.
- **Humongous Objects**: Large objects (spanning multiple regions) are allocated in dedicated “humongous” regions, typically treated as part of the old generation.
- **Remembered Sets**: G1GC maintains per-region remembered sets to track pointers from old to young regions, enabling independent young generation collections.

#### **Key Notes**
- The physical separation simplifies collection logic but requires careful region management to avoid fragmentation.
- G1GC’s pause times scale with the number of regions and remembered-set sizes, which can impact performance on very large heaps.

---

### **2. Generational ZGC**

#### **Heap Size Support**
- **Maximum Heap Size**: Generational ZGC supports heaps up to **16TB** in its current implementation (JDK 21), with potential to exceed this limit in future iterations due to the removal of multi-mapping (a constraint in non-generational ZGC).
- **Practical Range**: Designed for heaps from **hundreds of MB to multi-TB** (e.g., 512MB to 4TB+), excelling in large-scale, latency-sensitive applications.
- **Constraints**:
  - Generational ZGC’s concurrent design and colored pointers minimize pause times, making it highly scalable even for massive heaps.
  - Memory overhead is low (no multi-mapping, only metadata for colored pointers and double-buffered remembered sets), but concurrent operations increase CPU usage, which may require sufficient hardware resources.
- **Scalability Advantage**: Pause times remain sub-millisecond (<1ms) regardless of heap size, as most work (marking, relocation, compaction) is concurrent.

#### **Generational Separation**
- **Young and Old Generations**: Generational ZGC uses **logical separation** for young and old generations.
- **Heap Structure**:
  - The heap is a single contiguous space divided into fixed-size **pages** (e.g., 2MB on 64-bit systems).
  - **Young Generation**: Objects allocated recently are marked as belonging to the young generation via metadata (not physical regions).
  - **Old Generation**: Objects surviving multiple young collections are logically marked as old, residing in the same heap space.
  - **Logical Separation**: There’s no physical partitioning into young/old regions or pages. Instead, metadata (e.g., object tags or tables) tracks which objects belong to which generation. Pages can contain a mix of young and old objects, and the GC uses this metadata to collect them appropriately.
- **Large Objects**: Allocated in the young generation initially and promoted to old if they survive, without requiring special regions.
- **Double-Buffered Remembered Sets**: Track pointers from old to young objects, maintained concurrently to support young-only collections.

#### **Key Notes**
- Logical separation simplifies heap management and reduces fragmentation risks, as there’s no need to reserve separate memory pools for each generation.
- The use of colored pointers and concurrent collectors allows Generational ZGC to handle mixed young/old pages efficiently, but it adds complexity to the collection logic.

---

### **3. Shenandoah GC (Generational Mode)**

#### **Heap Size Support**
- **Maximum Heap Size**: Shenandoah supports heaps up to **64TB** on 64-bit systems, similar to G1GC, with no hard limit beyond available memory and JVM constraints.
- **Practical Range**: Effective for heaps from **hundreds of MB to tens of TB** (e.g., 1GB to 1TB+), though generational mode is still experimental in JDK 21 and less tested at extreme scales.
- **Constraints**:
  - Shenandoah’s concurrent evacuation and load reference barriers (LRBs) keep pause times low, but generational mode introduces remembered-set overhead, which can impact performance on very large heaps.
  - Supports 32-bit systems and compressed pointers, offering flexibility for smaller heaps or constrained environments.
- **Scalability Advantage**: Pause times are typically below 10ms (soft goal), even for large heaps, due to concurrent marking and evacuation.

#### **Generational Separation**
- **Young and Old Generations**: Generational Shenandoah uses **logical separation** for young and old generations, with some physical aspects due to region-based management.
- **Heap Structure**:
  - The heap is divided into equal-sized **regions** (e.g., 256KB to 2MB, depending on heap size).
  - **Young Generation**: A subset of regions is designated for new allocations (Eden-like) and survivor objects, collected frequently.
  - **Old Generation**: Other regions hold promoted, long-lived objects, collected less often.
  - **Logical Separation with Physical Elements**:
    - Regions are logically grouped into young or old generations based on their contents, but a region typically contains objects of one generation at a time (e.g., young regions for new allocations, old regions for promoted objects).
    - Unlike G1GC’s strict physical partitioning, Shenandoah’s regions can dynamically shift between generations, and the GC relies on metadata to track object ages and intergenerational pointers.
    - However, the region-based design means young and old objects are often in separate regions, giving a semi-physical separation compared to ZGC’s fully logical approach.
- **Brooks Pointers (LRBs)**: Each object has an extra word (forwarding pointer) to support concurrent evacuation, used to update references during relocation.
- **Card-Table Remembered Sets**: Track pointers from old to young regions, maintained concurrently to enable young-only collections.

#### **Key Notes**
- The semi-physical, region-based separation balances flexibility and efficiency, allowing Shenandoah to collect young regions independently while minimizing fragmentation.
- Generational mode (experimental in JDK 21) adds complexity to remembered-set management, which may evolve as it matures toward production readiness.

---

### **Comparison Table**

| **Garbage Collector** | **Max Heap Size** | **Practical Heap Range** | **Young/Old Separation** | **Separation Details** |
|-----------------------|-------------------|--------------------------|--------------------------|-----------------------|
| **G1GC**             | 64TB (with CompressedOops) | Hundreds of MB to tens of GB (e.g., 1GB–32GB, up to 100GB+) | Physical | Young (Eden, Survivor) and old generations in distinct heap regions. Regions can switch roles dynamically. |
| **Generational ZGC** | 16TB (potentially higher) | Hundreds of MB to multi-TB (e.g., 512MB–4TB+) | Logical | Single contiguous heap with pages; young/old tracked via metadata. Pages can mix generations. |
| **Shenandoah GC (Generational)** | 64TB | Hundreds of MB to tens of TB (e.g., 1GB–1TB+) | Logical (semi-physical) | Region-based heap; young/old logically separated, but regions often hold one generation. Regions shift roles dynamically. |

---

### **Detailed Analysis**

1. **Heap Size Support**:
   - **G1GC**: Well-suited for medium to large heaps (1GB–100GB), with theoretical support up to 64TB. Performance may degrade without tuning on very large heaps due to pause-time scaling with region count.
   - **Generational ZGC**: Optimized for large heaps (up to 16TB, potentially more in future), with sub-millisecond pauses regardless of size. Its concurrent design and lack of multi-mapping make it ideal for multi-TB workloads.
   - **Shenandoah GC**: Matches G1GC’s theoretical 64TB limit and supports a wide range (MB to TB). Generational mode’s experimental status means real-world limits are less tested, but its design scales well for large heaps with low pause times.

2. **Young/Old Separation**:
   - **G1GC’s Physical Separation**: Simplifies young generation collections by isolating Eden/Survivor regions but requires careful region allocation to avoid fragmentation. Remembered sets add overhead for tracking intergenerational pointers.
   - **Generational ZGC’s Logical Separation**: Offers maximum flexibility, as young and old objects coexist in the same pages. Metadata-driven collection avoids physical partitioning, reducing fragmentation risks but increasing complexity in tracking object generations.
   - **Shenandoah’s Logical (Semi-Physical) Separation**: Strikes a middle ground. Regions provide some physical separation (young vs. old regions), but logical tracking allows regions to switch roles. This balances efficiency and adaptability but adds remembered-set overhead in generational mode.

3. **Implications**:
   - **Performance**: G1GC’s physical separation can lead to faster young collections but risks fragmentation. ZGC’s logical separation minimizes fragmentation and scales better for large heaps. Shenandoah’s hybrid approach offers flexibility but may incur higher overhead until optimized.
   - **Use Cases**:
     - **G1GC**: General-purpose, good for latency-tolerant applications with medium-to-large heaps.
     - **Generational ZGC**: Ideal for latency-sensitive, large-scale applications (e.g., cloud services, big data).
     - **Shenandoah**: Suited for low-latency workloads with varying heap sizes, especially once generational mode matures.

---

### **Conclusion**
- **Heap Size**: All three collectors support massive heaps (16TB–64TB), but **Generational ZGC** stands out for its proven scalability to multi-TB heaps with ultra-low pauses. **G1GC** and **Shenandoah** are comparable in theoretical limits, though G1GC is more battle-tested for large heaps, and Shenandoah’s generational mode needs further validation.
- **Generational Separation**: **G1GC** uses physical separation for clear young/old boundaries, while **Generational ZGC** relies on fully logical separation for flexibility. **Shenandoah** blends logical and semi-physical separation, leveraging regions for efficiency but with added complexity.

Choose **G1GC** for robust, general-purpose GC with predictable performance, **Generational ZGC** for cutting-edge, low-latency performance on massive heaps, or **Shenandoah** for experimental low-latency with potential for future enhancements.
