package com.nickrobison.tuple;

import com.nickrobison.tuple.unsafe.Coterie;
import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolSettings;
import nf.fr.eraasoft.pool.PoolableObjectBase;
import org.openjdk.jmh.annotations.Benchmark;
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
    private static final Unsafe unsafe = Coterie.unsafe();
    private BlockingQueue<Container> containers;
    private PoolSettings<Container> poolSettings = new PoolSettings<>(
            new PoolableObjectBase<Container>() {
                @Override
                public Container make() {
                    return new Container(0, 0, (short) 0);
                }

                @Override
                public void activate(Container container) {
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
            containers = new ArrayBlockingQueue<>(100);
            containers.offer(new Container(0, 0, (short) 0));
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
            pool2 = new FastObjectPool<>(() -> new Container(0, 0, (short) 0), 10);
            pool3 = new TuplePool<>(10, false,
                    size -> {
                        Container[] ary = new Container[size];
                        for (int i = 0; i < ary.length; i++) {
                            ary[i] = new Container(0, 0, (short) 0);
                        }
                        return ary;
                    },
                    ary -> {

                    }
            );

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
        } catch (Exception ignored) {

        }
    }

    @Benchmark
    public long testAllocateSetAndDeallocate() {
        long record = schema.createRecord();
        schema.setLong(record, 0, 100);
        schema.setInt(record, 1, 200);
        schema.setShort(record, 2, (short)300);

        long r = schema.getLong(record, 0) + schema.getInt(record, 1) + schema.getShort(record, 2);
        schema.destroy(record);
        return r;
    }

    @Benchmark
    public long testOffheapSchemaSet() {
        schema.setLong(record2, 0, 100);
        schema.setInt(record2, 1, 200);
        schema.setShort(record2, 2, (short)300);
        return schema.getLong(record2, 0) + schema.getInt(record2, 1) + schema.getShort(record2, 2);
    }

    @Benchmark
public long testOffheapAllocateAndSet() {
        long record = unsafe.allocateMemory(8 + 4 + 2L);
    unsafe.putLong(record, 100);
    unsafe.putInt(record+8, 200);
    unsafe.putShort(record+12, (short)300);
    long r = unsafe.getLong(record) + unsafe.getInt(record+8) + unsafe.getShort(record+12);
    unsafe.freeMemory(record);
    return r;
}

    @Benchmark
    public long testOffheapDirectSet() {
        unsafe.putLong(record2, 100);
        unsafe.putInt(record2 + 8L, 200);
        unsafe.putShort(record2 + 12L, (short)300);
        return unsafe.getLong(record2) + unsafe.getInt(record2 + 8L) + unsafe.getShort(record2 + 12L);
    }

    @Benchmark
    public long testInvokeDynamic() throws Throwable {
        Container container = new Container(0,0,(short)0);
        mhsa.dynamicInvoker().invoke(container, 100L);
        mhsb.dynamicInvoker().invoke(container, 200);
        mhsc.dynamicInvoker().invoke(container, (short)300L);
        return (Long)mhga.dynamicInvoker().invoke(container) + (Integer)mhgb.dynamicInvoker().invoke(container) + (Short)mhgc.dynamicInvoker().invoke(container);
    }

    @Benchmark
    public long testStormTuple() {
        List<Long> list = new ArrayList<>();
        list.add(100L);
        list.add(200L);
        list.add(300L);
        return list.get(0) + list.get(1) + list.get(2);
    }

    @Benchmark
    public long testLongArray() {
        long[] longs = new long[3];
        longs[0] = 100L;
        longs[1] = 200;
        longs[2] = (short)300;
        return longs[0] + longs[1] + longs[2];
    }

    @Benchmark
    public long testClass() {
        Container container = new Container(0, 0, (short)0);
        container.a = 100;
        container.b = 200;
        container.c = 300;
        return container.a + container.b + container.c;
    }

    @Benchmark
    public long testReflectField() throws Exception {
        Container container = new Container(0, 0, (short)0);
        fieldA.setLong(container, 100);
        fieldB.setInt(container, 200);
        fieldC.setShort(container, (short)300);
        return fieldA.getLong(container) + fieldB.getInt(container) + fieldC.getShort(container);
    }

    @Benchmark
    public long testQueuedObject() throws InterruptedException {
        Container container = containers.take();
        container.a = 100;
        container.b = 200;
        container.c = 300;
        long r = container.a + container.b + container.c;
        containers.offer(container);
        return r;
    }

    @Benchmark
    public long testPooledObject() throws Exception {
        Container container = pool.getObj();
        container.a = 100;
        container.b = 200;
        container.c = 300;
        long l = container.a + container.b + container.c;
        pool.returnObj(container);
        return l;
    }

    @Benchmark
    public long testFastPool() throws Exception {
        FastObjectPool.Holder<Container> holder = pool2.take();
        Container container = holder.getValue();
        container.a = 100;
        container.b = 200;
        container.c = 300;
        long r = container.a + container.b + container.c;
        pool2.release(holder);
        return r;
    }

    @Benchmark
    public long testTuplePool() {
        Container container = pool3.checkout();
        container.a = 100;
        container.b = 200;
        container.c = 300;
        long r = container.a + container.b + container.c;
        pool3.release(container);
        return r;
    }

    @Benchmark
    public long testFastTuplePreAllocIndexedBoxing() {
        FastTuple tuple = schema.createTuple(record2);
        tuple.set(1, 100L);
        tuple.set(2, 200);
        tuple.set(3, (short) 300);
        return (Long) tuple.get(1) + (Integer) tuple.get(2) + (Short) tuple.get(3);
    }

    @Benchmark
    public long testFastTuplePreAllocIndexed() {
        FastTuple tuple = schema.createTuple(record2);
        tuple.setLong(1, 100L);
        tuple.setInt(2, 200);
        tuple.setShort(3, (short) 300);
        return tuple.getLong(1) + tuple.getInt(2) + tuple.getShort(3);
    }

    @Benchmark
    public long testFastTupleStaticBinding() {
        StaticBinding tuple = (StaticBinding) schema.createTuple(record2);
        tuple.a(100L);
        tuple.b(200);
        tuple.c((short) 300);
        return tuple.a() + tuple.b() + tuple.c();
    }


    public interface StaticBinding {
        void a(long a);

        void b(int b);

        void c(short c);

        long a();

        int b();

        short c();
    }

}


