# FastTuple

This repository is [forked from](https://github.com/boundary/fasttuple), all credit to Cliff Moon and his team, it's a fantastic piece of work.
I've made a few changes and pushed everything up to Maven. Enjoy!  

## Introduction

There are lots of good things about working on the JVM, like a world class JIT, operating system threads, and a world class garbage collector.  However, one limiting factor can often be the interaction between primitive types and referenced types in Java.  Primitive types are the built in types that represent integral numbers, floating point numbers, and boolean yes/no values.  Primitives are also quite fast and memory efficient: they get allocated either on the stack if they're being used in a method, or inlined in an object when they're declared as field members.  And they wind up being fast because the JIT can often optimize their access down to a single CPU instruction.  This works really well when you know what types a class will hold as its state beforehand.  If, on the other hand, you don't know what an object or array will hold at compile time the JVM forces you to box primitives.  Boxing means that the primitives get wrapped in a heap allocated object, and their container will hold a reference to them.  That type of overhead winds up being inefficient both in access time and memory space.  Access time suffers because this type of layout breaks locality of reference, which is the principle that memory frequently accessed together should be stored adjacently.  All modern memory hierarchies optimize for locality of reference in their caching implementations.  The extra allocations and garbage generated also put pressure on the JVM's garbage collector, which can often be a cause of long pause times.

We wrote FastTuple to try and help solve this problem.  FastTuple generates heterogeneous collections of primitive values and ensures as best it can that they will be laid out adjacently in memory.  The individual values in the tuple can either be accessed from a statically bound interface, via an indexed accessor, or via reflective or other dynamic invocation techniques.  FastTuple is designed to deal with a large number of tuples therefore it will also attempt to pool tuples such that they do not add significantly to the GC load of a system.  FastTuple is also capable of allocating the tuple value storage entirely off-heap, using Java's direct memory capabilities.

FastTuple pulls off its trick via runtime bytecode generation.  The user supplies it with a schema of field names and types.  That schema is then built into a Java class definition which will contain accessor methods and either field definitions or the memory address for an off heap allocation, depending on which storage method was requested.  The resulting Java class gets compiled into bytecode and then loaded as a reflective Class object.  This Class object can then be used to create instances of the new class.

## Note on GPG

You'll also need to ensure that you have GPG available on the command line. Mac OS X and Windows users may need to install via (https://gpgtools.org/) or cygwin respectively.

## Usage

Interaction with FastTuple primarily takes place via the TupleSchema class.  Each instance of TupleSchema describes a separate type of FastTuple both from the perspective of the FastTuple library and the JVM.  At this time, allowable field types are the primitive classes for: long, int, short, char, byte, float, double.  Support for String is planned for a later release.  Some examples:

### Heap Allocated Tuples

```java
	TupleSchema schema = TupleSchema.builder().
		addField("fieldA", Long.TYPE).
		addField("fieldB", Int.TYPE).
		addField("fieldC", Short.TYPE).
		heapMemory().
		build();

	//creates a new tuple allocated on the JVM heap
	FastTuple tuple = schema.createTuple();
```

### Direct Allocated Tuples

```java
	TupleSchema schema = TupleSchema.builder().
		addField("fieldA", Long.TYPE).
		addField("fieldB", Int.TYPE).
		addField("fieldC", Short.TYPE).
		directMemory().
		build();

	//creates a new tuple, allocating memory off heap
	FastTuple tuple = schema.createTuple();
	//do some stuff
	tuple.setLong(1, 10000L);
	tuple.setInt(2, 50);
	tuple.setShort(3, (short)256);
	//if you don't destroy the tuple you are leaking memory
	schema.destroy(tuple);
```

### Aligning Direct Allocated Tuples

Direct allocated tuples can be aligned such that they do not share cache lines.  This is useful for situations where
adjacent tuples are manipulated concurrently by different threads: an adequately padded FastTuple can eliminate false sharing in the CPU cache architecture.  Veriifying that property, as expected, will require extensive benchmarking inside of the target system.

```java
	TupleSchema schema = TupleSchema.builder().
		addField("fieldA", Long.TYPE).
		addField("fieldB", Int.TYPE).
		addField("fieldC", Short.TYPE).
		directMemory().
		padToWordSize(64).
		build();

	//creates a new tuple, allocating memory off heap
	FastTuple tuple = schema.createTuple();
	//do some stuff
	tuple.setLong(1, 10000L);
	tuple.setInt(2, 50);
	tuple.setShort(3, (short)256);
	//if you don't destroy the tuple you are leaking memory
	schema.destroy(tuple);
```

### Utilizing Tuple Pools

Each schema will allocate a tuple pool per accessing thread if a poolSize is set.

```java
	TupleSchema schema = TupleSchema.builder().
		addField("fieldA", Long.TYPE).
		addField("fieldB", Int.TYPE).
		addField("fieldC", Short.TYPE).
		poolOfSize(1024).
		//allocates an extra poolOfSize records when the pool is empty
		.expandingPool().
		build();

	//checks a tuple from the pool
	FastTuple tuple = schema.pool().checkout();
	//do some stuff
	tuple.setLong(1, 10000L);
	tuple.setInt(2, 50);
	tuple.setShort(3, (short)256);
	//if you don't check the tuple back in you either leak memory or objects. bad dog.
	schema.pool().release(tuple);
```

## Performance

One of the main goals of this library is performance.  Toward that end it has a full suite of microbenchmarks to test the various supported means of accessing and manipulating tuples for both tuning the library and showing the tradeoffs in overhead for things like pooling and allocating tuples on demand.  Here's what a full run looks like on a late 2013 macbook pro 2.6ghz with Java 8 1.8.0_05-b13.

```
Benchmark                                                                              Mode   Samples         Mean   Mean error    Units
c.b.t.AccessMethodBenchmark.testAllocateSetAndDeallocate                              thrpt        20     7069.954      181.689   ops/ms
c.b.t.AccessMethodBenchmark.testClass                                                 thrpt        20  1628847.332    21756.564   ops/ms
c.b.t.AccessMethodBenchmark.testFastPool                                              thrpt        20    30715.791      570.086   ops/ms
c.b.t.AccessMethodBenchmark.testFastTuplePreAllocIndexed                              thrpt        20   160173.234     2498.380   ops/ms
c.b.t.AccessMethodBenchmark.testFastTuplePreAllocIndexedBoxing                        thrpt        20    59117.984      630.813   ops/ms
c.b.t.AccessMethodBenchmark.testFastTupleStaticBinding                                thrpt        20   157928.877     2411.094   ops/ms
c.b.t.AccessMethodBenchmark.testInvokeDynamic                                         thrpt        20    26085.594     1158.292   ops/ms
c.b.t.AccessMethodBenchmark.testLongArray                                             thrpt        20  1721846.666    23422.990   ops/ms
c.b.t.AccessMethodBenchmark.testOffheapAllocateAndSet                                 thrpt        20     7512.737      167.141   ops/ms
c.b.t.AccessMethodBenchmark.testOffheapDirectSet                                      thrpt        20   898743.426    37659.166   ops/ms
c.b.t.AccessMethodBenchmark.testOffheapSchemaSet                                      thrpt        20   367841.110     4282.620   ops/ms
c.b.t.AccessMethodBenchmark.testPooledObject                                          thrpt        20    12572.464      155.458   ops/ms
c.b.t.AccessMethodBenchmark.testQueuedObject                                          thrpt        20    25092.751      242.771   ops/ms
c.b.t.AccessMethodBenchmark.testReflectField                                          thrpt        20    28850.478      275.462   ops/ms
c.b.t.AccessMethodBenchmark.testStormTuple                                            thrpt        20    79693.517      888.956   ops/ms
c.b.t.AccessMethodBenchmark.testTuplePool                                             thrpt        20    70790.084      950.775   ops/ms
c.b.t.FastTupleBenchmarks.DirectBenchmarks.measureDirectSchemaAllocate                thrpt        20     7214.300       98.387   ops/ms
c.b.t.FastTupleBenchmarks.DirectBenchmarks.measureDirectSchemaDeque                   thrpt        20   153937.210     2534.986   ops/ms
c.b.t.FastTupleBenchmarks.DirectBenchmarks.measureDirectSchemaPoolEval                thrpt        20    53563.512      836.586   ops/ms
c.b.t.FastTupleBenchmarks.DirectBenchmarks.measureDirectSchemaPoolIface               thrpt        20    57669.480      716.962   ops/ms
c.b.t.FastTupleBenchmarks.DirectBenchmarks.measureDirectSchemaPoolIndexed             thrpt        20    57633.447     1025.338   ops/ms
c.b.t.FastTupleBenchmarks.DirectBenchmarks.measureDirectSchemaPoolIndexedBoxed        thrpt        20    38984.939      823.182   ops/ms
c.b.t.FastTupleBenchmarks.DirectBenchmarks.measureDirectSchemaPreallocEval            thrpt        20   332142.591     6426.566   ops/ms
c.b.t.FastTupleBenchmarks.DirectBenchmarks.measureDirectSchemaPreallocIface           thrpt        20   370593.271     5369.162   ops/ms
c.b.t.FastTupleBenchmarks.DirectBenchmarks.measureDirectSchemaPreallocIndexed         thrpt        20   377811.881     7077.471   ops/ms
c.b.t.FastTupleBenchmarks.DirectBenchmarks.measureDirectSchemaPreallocIndexedBoxed    thrpt        20   115699.056     2402.657   ops/ms
c.b.t.FastTupleBenchmarks.HeapBenchmarks.measureHeapSchemaAllocate                    thrpt        20   990788.476    12295.764   ops/ms
c.b.t.FastTupleBenchmarks.HeapBenchmarks.measureHeapSchemaDeque                       thrpt        20   173527.417     3619.733   ops/ms
c.b.t.FastTupleBenchmarks.HeapBenchmarks.measureHeapSchemaPoolEval                    thrpt        20    59377.121     1071.014   ops/ms
c.b.t.FastTupleBenchmarks.HeapBenchmarks.measureHeapSchemaPoolIface                   thrpt        20    62391.209      765.821   ops/ms
c.b.t.FastTupleBenchmarks.HeapBenchmarks.measureHeapSchemaPoolIndexed                 thrpt        20    62120.412     1105.981   ops/ms
c.b.t.FastTupleBenchmarks.HeapBenchmarks.measureHeapSchemaPoolIndexedBoxed            thrpt        20    42187.554      737.124   ops/ms
c.b.t.FastTupleBenchmarks.HeapBenchmarks.measureHeapSchemaPreallocEval                thrpt        20   397337.746     5274.807   ops/ms
c.b.t.FastTupleBenchmarks.HeapBenchmarks.measureHeapSchemaPreallocEvalField           thrpt        20   579136.768     8818.827   ops/ms
c.b.t.FastTupleBenchmarks.HeapBenchmarks.measureHeapSchemaPreallocIface               thrpt        20   554449.236     6740.073   ops/ms
c.b.t.FastTupleBenchmarks.HeapBenchmarks.measureHeapSchemaPreallocIndexed             thrpt        20   536258.735    10417.522   ops/ms
c.b.t.FastTupleBenchmarks.HeapBenchmarks.measureHeapSchemaPreallocIndexedBoxed        thrpt        20   202220.576     3806.694   ops/ms
```

To run the benchmarks:

```
mvn package
java -jar fasttuple-bench/target/microbenchmarks.jar
```
