package com.boundary.tuple;

import com.boundary.tuple.codegen.TupleExpression;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Created by cliff on 5/12/14.
 */
public class FastTupleBenchmarks {

    @State(Scope.Benchmark)
    public static class DirectSchema {
        public DirectTupleSchema schema;
        public TupleExpression.Evaluator eval1;
        public TupleExpression.LongEvaluator eval2;

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
        }
    }

    @State(Scope.Benchmark)
    public static class HeapSchema {
        public HeapTupleSchema schema;
        public TupleExpression.Evaluator eval1;
        public TupleExpression.LongEvaluator eval2;
        public TupleExpression.Evaluator eval3;
        public TupleExpression.LongEvaluator eval4;

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
        public void measureDirectSchemaPoolIndexedBoxed(DirectSchema ds) throws Exception {
            FastTuple tuple = ds.schema.pool().checkout();

            tuple.set(1, 100L);
            tuple.set(2, 200);
            tuple.set(3, (short)300);

            if ((Long)tuple.get(1) + (Integer)tuple.get(2) + (Short)tuple.get(3) == System.nanoTime()) throw new IllegalStateException();
            ds.schema.pool().release(tuple);
        }

        @GenerateMicroBenchmark
        public void measureDirectSchemaPoolIndexed(DirectSchema ds) throws Exception {
            FastTuple tuple = ds.schema.pool().checkout();

            tuple.setLong(1, 100L);
            tuple.setInt(2, 200);
            tuple.setShort(3, (short)300);

            if (tuple.getLong(1) + tuple.getInt(2) + tuple.getShort(3) == System.nanoTime()) throw new IllegalStateException();
            ds.schema.pool().release(tuple);
        }

        @GenerateMicroBenchmark
        public void measureDirectSchemaPoolEval(DirectSchema ds) throws Exception {
            FastTuple tuple = ds.schema.pool().checkout();

            ds.eval1.evaluate(tuple);
            if (ds.eval2.evaluate(tuple) == System.nanoTime()) throw new IllegalStateException();
            ds.schema.pool().release(tuple);
        }

        @GenerateMicroBenchmark
        public void measureDirectSchemaPoolStatic(DirectSchema ds) throws Exception {
            StaticBinding tuple = (StaticBinding)ds.schema.pool().checkout();

            tuple.a(100L);
            tuple.b(200);
            tuple.c((short)300);

            if (tuple.a() + tuple.b() + tuple.c() == System.nanoTime()) throw new IllegalStateException();
            ds.schema.pool().release((FastTuple)tuple);
        }
    }

    public static class HeapBenchmarks {
        @GenerateMicroBenchmark
        public void measureHeapSchemaPoolIndexedBoxed(HeapSchema hs) throws Exception {
            FastTuple tuple = hs.schema.pool().checkout();

            tuple.set(1, 100L);
            tuple.set(2, 200);
            tuple.set(3, (short)300);

            if ((Long)tuple.get(1) + (Integer)tuple.get(2) + (Short)tuple.get(3) == System.nanoTime()) throw new IllegalStateException();
            hs.schema.pool().release(tuple);
        }

        @GenerateMicroBenchmark
        public void measureHeapSchemaPoolIndexed(HeapSchema hs) throws Exception {
            FastTuple tuple = hs.schema.pool().checkout();

            tuple.setLong(1, 100L);
            tuple.setInt(2, 200);
            tuple.setShort(3, (short)300);

            if (tuple.getLong(1) + tuple.getInt(2) + tuple.getShort(3) == System.nanoTime()) throw new IllegalStateException();
            hs.schema.pool().release(tuple);
        }

        @GenerateMicroBenchmark
        public void measureHeapSchemaPoolEval(HeapSchema hs) throws Exception {
            FastTuple tuple = hs.schema.pool().checkout();

            hs.eval1.evaluate(tuple);
            if (hs.eval2.evaluate(tuple) == System.nanoTime()) throw new IllegalStateException();
            hs.schema.pool().release(tuple);
        }

        @GenerateMicroBenchmark
        public void measureHeapSchemaPoolStatic(HeapSchema hs) throws Exception {
            StaticBinding tuple = (StaticBinding)hs.schema.pool().checkout();

            tuple.a(100L);
            tuple.b(200);
            tuple.c((short)300);

            if (tuple.a() + tuple.b() + tuple.c() == System.nanoTime()) throw new IllegalStateException();
            hs.schema.pool().release((FastTuple)tuple);
        }
    }
}
