package com.boundary.tuple;

import com.boundary.tuple.unsafe.Coterie;
import com.google.common.base.Function;
import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolSettings;
import nf.fr.eraasoft.pool.PoolableObjectBase;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import other.FastObjectPool;
import sun.misc.Unsafe;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@State(Scope.Benchmark)
public class AccessMethodBenchmark {
    private DirectTupleSchema schema;
    private BlockingQueue<Container> containers;
    private PoolSettings<Container> poolSettings = new PoolSettings<Container>(
            new PoolableObjectBase<Container>() {
                @Override
                public Container make() throws PoolException {
                    return new Container(0,0,(short)0);
                }

                @Override
                public void activate(Container container) throws PoolException {
                    container.a = 0;
                    container.b = 0;
                    container.c = 0;
                }
            }
    );
    private FastObjectPool<Container> pool2;
    TuplePool<Container> pool3;
    ObjectPool<Container> pool;
    long record2;
    Field fieldA;
    Field fieldB;
    Field fieldC;
    ConstantCallSite mhsa;
    ConstantCallSite mhsb;
    ConstantCallSite mhsc;
    ConstantCallSite mhga;
    ConstantCallSite mhgb;
    ConstantCallSite mhgc;


    public AccessMethodBenchmark() {
        try {
            containers = new ArrayBlockingQueue<Container>(100);
            containers.offer(new Container(0,0,(short)0));
            schema = TupleSchema.builder().
                    addField("a", Long.TYPE).
                    addField("b", Integer.TYPE).
                    addField("c", Short.TYPE).
                    implementInterface(StaticBinding.class).
                    directMemory().
                    build();
            record2 = schema.createRecord();
            poolSettings.min(1).max(10);
            pool = poolSettings.pool();
            pool2 = new FastObjectPool<Container>(new FastObjectPool.PoolFactory<Container>() {
                @Override
                public Container create() {
                    return new Container(0,0,(short)0);
                }
            }, 10);
            pool3 = new TuplePool<Container>(10, false, new Function<Integer, Container[]>() {
                @Override
                public Container[] apply(Integer size) {
                    Container[] ary = new Container[size];
                    for (int i=0; i<ary.length; i++) {
                        ary[i] = new Container(0,0,(short)0);
                    }
                    return ary;
                }
            });

            fieldA = Container.class.getDeclaredField("a");
            fieldB = Container.class.getDeclaredField("b");
            fieldC = Container.class.getDeclaredField("c");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            mhsa = new ConstantCallSite(lookup.findSetter(Container.class, "a", Long.TYPE));
            mhsb = new ConstantCallSite(lookup.findSetter(Container.class, "b", Integer.TYPE));
            mhsc = new ConstantCallSite(lookup.findSetter(Container.class, "c", Short.TYPE));
            mhga = new ConstantCallSite(lookup.findGetter(Container.class, "a", Long.TYPE));
            mhgb = new ConstantCallSite(lookup.findGetter(Container.class, "b", Integer.TYPE));
            mhgc = new ConstantCallSite(lookup.findGetter(Container.class, "c", Short.TYPE));
        } catch (Exception ex) {

        }
    }

    @GenerateMicroBenchmark
    public void testAllocateSetAndDeallocate() {
        long record = schema.createRecord();
        schema.setLong(record, 0, 100);
        schema.setInt(record, 1, 200);
        schema.setShort(record, 2, (short)300);
        if (schema.getLong(record, 0) + schema.getInt(record, 1) + schema.getShort(record, 2) == System.nanoTime())
            throw new IllegalStateException();
        schema.destroy(record);
    }

    @GenerateMicroBenchmark
    public void testOffheapSchemaSet() {
        schema.setLong(record2, 0, 100);
        schema.setInt(record2, 1, 200);
        schema.setShort(record2, 2, (short)300);
        if (schema.getLong(record2, 0) + schema.getInt(record2, 1) + schema.getShort(record2, 2) == System.nanoTime())
            throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testOffheapDirectSet() {
        Unsafe un = Coterie.unsafe();
        un.putLong(record2 + 0L, 100);
        un.putInt(record2 + 8L, 200);
        un.putShort(record2 + 16L, (short)300);
        if (un.getLong(record2 + 0L) + un.getInt(record2 + 8L) + un.getShort(record2 + 16L) == System.nanoTime())
            throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testInvokeDynamic() throws Throwable {
        Container container = new Container(0,0,(short)0);
        mhsa.dynamicInvoker().invoke(container, 100L);
        mhsb.dynamicInvoker().invoke(container, 200);
        mhsc.dynamicInvoker().invoke(container, (short)300L);
        if ((Long)mhga.dynamicInvoker().invoke(container) + (Integer)mhgb.dynamicInvoker().invoke(container) + (Short)mhgc.dynamicInvoker().invoke(container) == System.nanoTime())
            throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testStormTuple() {
        List<Long> list = new ArrayList<Long>();
        list.add(100L);
        list.add(200L);
        list.add(300L);
        if (list.get(0) + list.get(1) + list.get(2) == System.nanoTime()) throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testLongArray() {
        long[] longs = new long[3];
        longs[0] = 100L;
        longs[1] = 200;
        longs[2] = (short)300;
        if (longs[0] + longs[1] + longs[2] == System.nanoTime()) throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testClass() {
        Container container = new Container(0, 0, (short)0);
        container.a = 100;
        container.b = 200;
        container.c = 300;
        if (container.a + container.b + container.c == System.nanoTime()) throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testReflectField() throws Exception {
        Container container = new Container(0, 0, (short)0);
        fieldA.setLong(container, 100);
        fieldB.setInt(container, 200);
        fieldC.setShort(container, (short)300);
        if (fieldA.getLong(container) + fieldB.getInt(container) + fieldC.getShort(container) == System.nanoTime()) throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testQueuedObject() throws InterruptedException {
        Container container = containers.take();
        container.a = 100;
        container.b = 200;
        container.c = 300;
        if (container.a + container.b + container.c == System.nanoTime()) throw new IllegalStateException();
        containers.offer(container);
    }

    @GenerateMicroBenchmark
    public void testPooledObject() throws Exception {
        Container container = pool.getObj();
        container.a = 100;
        container.b = 200;
        container.c = 300;
        if (container.a + container.b + container.c == System.nanoTime()) throw new IllegalStateException();
        pool.returnObj(container);
    }

    @GenerateMicroBenchmark
    public void testFastPool() throws Exception {
        FastObjectPool.Holder<Container> holder = pool2.take();
        Container container = holder.getValue();
        container.a = 100;
        container.b = 200;
        container.c = 300;
        if (container.a + container.b + container.c == System.nanoTime()) throw new IllegalStateException();
        pool2.release(holder);
    }

    @GenerateMicroBenchmark
    public void testTuplePool() throws Exception {
        Container container = pool3.checkout();
        container.a = 100;
        container.b = 200;
        container.c = 300;
        if (container.a + container.b + container.c == System.nanoTime()) throw new IllegalStateException();
        pool3.release(container);
    }

    @GenerateMicroBenchmark
    public void testFastTuplePreAllocIndexedBoxing() throws Exception {
        FastTuple tuple = schema.createTuple(record2);
        tuple.set(1, 100L);
        tuple.set(2, 200);
        tuple.set(3, (short) 300);
        if ((Long)tuple.get(1) + (Integer)tuple.get(2) + (Short)tuple.get(3) == System.nanoTime()) throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testFastTuplePreAllocIndexed() throws Exception {
        FastTuple tuple = schema.createTuple(record2);
        tuple.setLong(1, 100L);
        tuple.setInt(2, 200);
        tuple.setShort(3, (short) 300);
        if (tuple.getLong(1) + tuple.getInt(2) + tuple.getShort(3) == System.nanoTime()) throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testFastTupleStaticBinding() throws Exception {
        StaticBinding tuple = (StaticBinding)schema.createTuple(record2);
        tuple.a(100L);
        tuple.b(200);
        tuple.c((short)300);
        if (tuple.a() + tuple.b() + tuple.c() == System.nanoTime()) throw new IllegalStateException();
    }

    public static interface StaticBinding {
        public void a(long a);
        public void b(int b);
        public void c(short c);
        public long a();
        public int b();
        public short c();
    }

}


