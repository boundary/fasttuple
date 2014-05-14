package com.boundary.tuple;

import com.boundary.tuple.codegen.TupleExpression;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.ArrayDeque;

/**
 * Created by cliff on 5/12/14.
 */
public class FastTupleBenchmarks {

    @State(Scope.Benchmark)
    public static class DirectSchema {
        public DirectTupleSchema schema;
        public TupleExpression.Evaluator eval1;
        public TupleExpression.LongEvaluator eval2;
        public ArrayDeque<FastTuple> deque;
        public FastTuple tuple;

        @Setup
        public void setup() throws Exception {
            schema = TupleSchema.builder().
                    addField("a", Long.TYPE).
                    addField("b", Integer.TYPE).
                    addField("c", Short.TYPE).
                    implementInterface(StaticBinding.class).
                    poolOfSize(10).
                    directMemory().
                    build();
            eval1 = TupleExpression.builder().
                    expression("tuple.a(100L), tuple.b(200), tuple.c((short)300)").
                    schema(schema).
                    returnVoid();
            eval2 = TupleExpression.builder().
                    expression("tuple.a() + tuple.b() + tuple.c()").
                    schema(schema).
                    returnLong();

            deque = new ArrayDeque<>();
            for (FastTuple tuple : schema.createTupleArray(10)) {
                deque.push(tuple);
            }

            tuple = schema.createTuple();
        }
    }

    @State(Scope.Benchmark)
    public static class HeapSchema {
        public HeapTupleSchema schema;
        public TupleExpression.Evaluator eval1;
        public TupleExpression.LongEvaluator eval2;
        public TupleExpression.Evaluator eval3;
        public TupleExpression.LongEvaluator eval4;
        public ArrayDeque<FastTuple> deque;
        public FastTuple tuple;

        @Setup
        public void setup() throws Exception {
            schema = TupleSchema.builder().
                    addField("a", Long.TYPE).
                    addField("b", Integer.TYPE).
                    addField("c", Short.TYPE).
                    poolOfSize(10).
                    implementInterface(StaticBinding.class).
                    heapMemory().
                    build();
            eval1 = TupleExpression.builder().
                    expression("tuple.a(100L), tuple.b(200), tuple.c((short)300)").
                    schema(schema).
                    returnVoid();
            eval2 = TupleExpression.builder().
                    expression("tuple.a() + tuple.b() + tuple.c()").
                    schema(schema).
                    returnLong();

            eval3 = TupleExpression.builder().
                    expression("tuple.a = 100L, tuple.b = 200, tuple.c = (short)300").
                    schema(schema).
                    returnVoid();
            eval4 = TupleExpression.builder().
                    expression("tuple.a + tuple.b + tuple.c").
                    schema(schema).
                    returnLong();

            deque = new ArrayDeque<>();
            for (FastTuple tuple : schema.createTupleArray(10)) {
                deque.push(tuple);
            }

            tuple = schema.createTuple();
        }
    }

    public static interface StaticBinding {
        public void a(long a);
        public void b(int b);
        public void c(short c);
        public long a();
        public int b();
        public short c();
    }

    public static class DirectBenchmarks {
        @GenerateMicroBenchmark
        public long measureDirectSchemaAllocate(DirectSchema ds) throws Exception {
            FastTuple tuple = ds.schema.createTuple();
            long r = tuple.getLong(1);
            ds.schema.destroy(tuple);
            return r;
        }

        @GenerateMicroBenchmark
        public long measureDirectSchemaDeque(DirectSchema ds) throws Exception {
            FastTuple tuple = ds.deque.pop();

            tuple.setLong(1, 100L);
            tuple.setInt(2, 200);
            tuple.setShort(3, (short) 300);

            long r = tuple.getLong(1) + tuple.getInt(2) + tuple.getShort(3);
            ds.deque.push(tuple);
            return r;
        }

        @GenerateMicroBenchmark
        public long measureDirectSchemaPoolIndexedBoxed(DirectSchema ds) throws Exception {
            FastTuple tuple = ds.schema.pool().checkout();

            tuple.set(1, 100L);
            tuple.set(2, 200);
            tuple.set(3, (short)300);

            long r = (Long)tuple.get(1) + (Integer)tuple.get(2) + (Short)tuple.get(3);
            ds.schema.pool().release(tuple);
            return r;
        }

        @GenerateMicroBenchmark
        public long measureDirectSchemaPreallocIndexedBoxed(DirectSchema ds) throws Exception {
            FastTuple tuple = ds.tuple;

            tuple.set(1, 100L);
            tuple.set(2, 200);
            tuple.set(3, (short)300);

            return (Long)tuple.get(1) + (Integer)tuple.get(2) + (Short)tuple.get(3);
        }

        @GenerateMicroBenchmark
        public long measureDirectSchemaPoolIndexed(DirectSchema ds) throws Exception {
            FastTuple tuple = ds.schema.pool().checkout();

            tuple.setLong(1, 100L);
            tuple.setInt(2, 200);
            tuple.setShort(3, (short)300);

            long r = tuple.getLong(1) + tuple.getInt(2) + tuple.getShort(3);
            ds.schema.pool().release(tuple);
            return r;
        }

        @GenerateMicroBenchmark
        public long measureDirectSchemaPreallocIndexed(DirectSchema ds) throws Exception {
            FastTuple tuple = ds.tuple;

            tuple.setLong(1, 100L);
            tuple.setInt(2, 200);
            tuple.setShort(3, (short)300);

            return tuple.getLong(1) + tuple.getInt(2) + tuple.getShort(3);
        }

        @GenerateMicroBenchmark
        public long measureDirectSchemaPoolEval(DirectSchema ds) throws Exception {
            FastTuple tuple = ds.schema.pool().checkout();

            ds.eval1.evaluate(tuple);
            long r = ds.eval2.evaluate(tuple);
            ds.schema.pool().release(tuple);
            return r;
        }

        @GenerateMicroBenchmark
        public long measureDirectSchemaPreallocEval(DirectSchema ds) throws Exception {
            FastTuple tuple = ds.tuple;

            ds.eval1.evaluate(tuple);
            return ds.eval2.evaluate(tuple);
        }

        @GenerateMicroBenchmark
        public long measureDirectSchemaPoolStatic(DirectSchema ds) throws Exception {
            StaticBinding tuple = (StaticBinding)ds.schema.pool().checkout();

            tuple.a(100L);
            tuple.b(200);
            tuple.c((short)300);

            long r = tuple.a() + tuple.b() + tuple.c();
            ds.schema.pool().release((FastTuple)tuple);
            return r;
        }

        @GenerateMicroBenchmark
        public long measureDirectSchemaPreallocStatic(DirectSchema ds) throws Exception {
            StaticBinding tuple = (StaticBinding)ds.tuple;

            tuple.a(100L);
            tuple.b(200);
            tuple.c((short)300);

            return tuple.a() + tuple.b() + tuple.c();
        }
    }

    public static class HeapBenchmarks {
        @GenerateMicroBenchmark
        public long measureHeapSchemaAllocate(HeapSchema hs) throws Exception {
            FastTuple tuple = hs.schema.createTuple();

            return tuple.getLong(1);
        }

        @GenerateMicroBenchmark
        public long measureHeapSchemaDeque(HeapSchema hs) throws Exception {
            FastTuple tuple = hs.deque.pop();

            tuple.setLong(1, 100L);
            tuple.setInt(2, 200);
            tuple.setShort(3, (short) 300);

            long r = tuple.getLong(1) + tuple.getInt(2) + tuple.getShort(3);
            hs.deque.push(tuple);
            return r;
        }

        @GenerateMicroBenchmark
        public long measureHeapSchemaPoolIndexedBoxed(HeapSchema hs) throws Exception {
            FastTuple tuple = hs.schema.pool().checkout();

            tuple.set(1, 100L);
            tuple.set(2, 200);
            tuple.set(3, (short)300);

            long r = (Long)tuple.get(1) + (Integer)tuple.get(2) + (Short)tuple.get(3);
            hs.schema.pool().release(tuple);
            return r;
        }

        @GenerateMicroBenchmark
        public long measureHeapSchemaPreallocIndexedBoxed(HeapSchema hs) throws Exception {
            FastTuple tuple = hs.tuple;

            tuple.set(1, 100L);
            tuple.set(2, 200);
            tuple.set(3, (short)300);

            return (Long)tuple.get(1) + (Integer)tuple.get(2) + (Short)tuple.get(3);
        }

        @GenerateMicroBenchmark
        public long measureHeapSchemaPoolIndexed(HeapSchema hs) throws Exception {
            FastTuple tuple = hs.schema.pool().checkout();

            tuple.setLong(1, 100L);
            tuple.setInt(2, 200);
            tuple.setShort(3, (short)300);

            long r = tuple.getLong(1) + tuple.getInt(2) + tuple.getShort(3);
            hs.schema.pool().release(tuple);
            return r;
        }

        @GenerateMicroBenchmark
        public long measureHeapSchemaPreallocIndexed(HeapSchema hs) throws Exception {
            FastTuple tuple = hs.tuple;

            tuple.setLong(1, 100L);
            tuple.setInt(2, 200);
            tuple.setShort(3, (short)300);

            return tuple.getLong(1) + tuple.getInt(2) + tuple.getShort(3);
        }

        @GenerateMicroBenchmark
        public long measureHeapSchemaPoolEval(HeapSchema hs) throws Exception {
            FastTuple tuple = hs.schema.pool().checkout();

            hs.eval1.evaluate(tuple);
            long r = hs.eval2.evaluate(tuple);
            hs.schema.pool().release(tuple);
            return r;
        }

        @GenerateMicroBenchmark
        public long measureHeapSchemaPreallocEval(HeapSchema hs) throws Exception {
            FastTuple tuple = hs.tuple;

            hs.eval1.evaluate(tuple);
            return hs.eval2.evaluate(tuple);
        }

        @GenerateMicroBenchmark
        public long measureHeapSchemaPreallocEvalField(HeapSchema hs) throws Exception {
            FastTuple tuple = hs.tuple;

            hs.eval3.evaluate(tuple);
            return hs.eval4.evaluate(tuple);
        }

        @GenerateMicroBenchmark
        public long measureHeapSchemaPoolStatic(HeapSchema hs) throws Exception {
            StaticBinding tuple = (StaticBinding)hs.schema.pool().checkout();

            tuple.a(100L);
            tuple.b(200);
            tuple.c((short)300);

            long r = tuple.a() + tuple.b() + tuple.c();
            hs.schema.pool().release((FastTuple)tuple);
            return r;
        }

        @GenerateMicroBenchmark
        public long measureHeapSchemaPreallocStatic(HeapSchema hs) throws Exception {
            StaticBinding tuple = (StaticBinding)hs.tuple;

            tuple.a(100L);
            tuple.b(200);
            tuple.c((short)300);

            return tuple.a() + tuple.b() + tuple.c();
        }
    }
}
