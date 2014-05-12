# FastTuple

## Introduction

There are lots of good things about working on the JVM, like a world class JIT, operating system threads, and a world class garbage collector.  However, one limiting factor can often be the interaction between primitive types and referenced types in Java.  Primitive types are the built in types that represent integral numbers, floating point numbers, and boolean yes/no values.  Primitives are also quite fast and memory efficient: they get allocated either on the stack if they're being used in a method, or inlined in an object when they're declared as field members.  And they wind up being fast because the JIT can often optimize their access down to a single CPU instruction.  This works really well when you know what types a class will hold as its state beforehand.  If, on the other hand, you don't know what an object or array will hold at compile time the JVM forces you to box primitives.  Boxing means that the primitives get wrapped in a heap allocated object, and their container will hold a reference to them.  That type of overhead winds up being inefficient both in access time and memory space.  Access time suffers because this type of layout breaks locality of reference, which is the principle that memory frequently accessed together should be stored adjacently.  All modern memory hierarchies optimize for locality of reference in their caching implementations.  The extra allocations and garbage generated also put pressure on the JVM's garbage collector, which can often be a cause of long pause times.

We wrote FastTuple to try and help solve this problem.  FastTuple generates heterogeneous collections of primitive values and ensures as best it can that they will be laid out adjacently in memory.  The individual values in the tuple can either be accessed from a statically bound interface, via an indexed accessor, or via reflective or other dynamic invocation techniques.  FastTuple is designed to deal with a large number of tuples therefore it will also attempt to pool tuples such that they do not add significantly to the GC load of a system.  FastTuple is also capable of allocating the tuple value storage entirely off-heap, using Java's direct memory capabilities.

FastTuple pulls off its trick via runtime bytecode generation.  The user supplies it with a schema of field names and types.  That schema is then built into a Java class definition which will contain accessor methods and either field definitions or the memory address for an off heap allocation, depending on which storage method was requested.  The resulting Java class gets compiled into bytecode and then loaded as a reflective Class object.  This Class object can then be used to create instances of the new class.

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
	//if you don't check the tuple back in you either leak memory or objects. bad dog.
	schema.pool().release(tuple);