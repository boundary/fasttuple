/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.boundary;

import com.boundary.tuple.DirectTupleSchema;
import com.boundary.tuple.FastTuple;
import com.boundary.tuple.TuplePool;
import com.boundary.tuple.TupleSchema;
import com.boundary.tuple.unsafe.Coterie;
import com.google.common.base.Function;
import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;
import nf.fr.eraasoft.pool.PoolSettings;
import nf.fr.eraasoft.pool.PoolableObjectBase;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
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
    private BlockingQueue<Derp> derps;
    private PoolSettings<Derp> poolSettings = new PoolSettings<Derp>(
            new PoolableObjectBase<Derp>() {
                @Override
                public Derp make() throws PoolException {
                    return new Derp(0,0,(short)0);
                }

                @Override
                public void activate(Derp derp) throws PoolException {
                    derp.a = 0;
                    derp.b = 0;
                    derp.c = 0;
                }
            }
    );
    private FastObjectPool<Derp> pool2;
    TuplePool<Derp> pool3;
    ObjectPool<Derp> pool;
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
            derps = new ArrayBlockingQueue<Derp>(100);
            derps.offer(new Derp(0,0,(short)0));
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
            pool2 = new FastObjectPool<Derp>(new FastObjectPool.PoolFactory<Derp>() {
                @Override
                public Derp create() {
                    return new Derp(0,0,(short)0);
                }
            }, 10);
            pool3 = new TuplePool<Derp>(10, false, new Function<Integer, Derp[]>() {
                @Override
                public Derp[] apply(Integer size) {
                    return new Derp[size];
                }
            });

            fieldA = Derp.class.getDeclaredField("a");
            fieldB = Derp.class.getDeclaredField("b");
            fieldC = Derp.class.getDeclaredField("c");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            mhsa = new ConstantCallSite(lookup.findSetter(Derp.class, "a", Long.TYPE));
            mhsb = new ConstantCallSite(lookup.findSetter(Derp.class, "b", Integer.TYPE));
            mhsc = new ConstantCallSite(lookup.findSetter(Derp.class, "c", Short.TYPE));
            mhga = new ConstantCallSite(lookup.findGetter(Derp.class, "a", Long.TYPE));
            mhgb = new ConstantCallSite(lookup.findGetter(Derp.class, "b", Integer.TYPE));
            mhgc = new ConstantCallSite(lookup.findGetter(Derp.class, "c", Short.TYPE));
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
        Derp derp = new Derp(0,0,(short)0);
        mhsa.dynamicInvoker().invoke(derp, 100L);
        mhsb.dynamicInvoker().invoke(derp, 200);
        mhsc.dynamicInvoker().invoke(derp, (short)300L);
        if ((Long)mhga.dynamicInvoker().invoke(derp) + (Integer)mhgb.dynamicInvoker().invoke(derp) + (Short)mhgc.dynamicInvoker().invoke(derp) == System.nanoTime())
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
        Derp derp = new Derp(0, 0, (short)0);
        derp.a = 100;
        derp.b = 200;
        derp.c = 300;
        if (derp.a + derp.b + derp.c == System.nanoTime()) throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testReflectField() throws Exception {
        Derp derp = new Derp(0, 0, (short)0);
        fieldA.setLong(derp, 100);
        fieldB.setInt(derp, 200);
        fieldC.setShort(derp, (short)300);
        if (fieldA.getLong(derp) + fieldB.getInt(derp) + fieldC.getShort(derp) == System.nanoTime()) throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testQueuedObject() throws InterruptedException {
        Derp derp = derps.take();
        derp.a = 100;
        derp.b = 200;
        derp.c = 300;
        if (derp.a + derp.b + derp.c == System.nanoTime()) throw new IllegalStateException();
        derps.offer(derp);
    }

    @GenerateMicroBenchmark
    public void testPooledObject() throws Exception {
        Derp derp = pool.getObj();
        derp.a = 100;
        derp.b = 200;
        derp.c = 300;
        if (derp.a + derp.b + derp.c == System.nanoTime()) throw new IllegalStateException();
        pool.returnObj(derp);
    }

    @GenerateMicroBenchmark
    public void testFastPool() throws Exception {
        FastObjectPool.Holder<Derp> holder = pool2.take();
        Derp derp = holder.getValue();
        derp.a = 100;
        derp.b = 200;
        derp.c = 300;
        if (derp.a + derp.b + derp.c == System.nanoTime()) throw new IllegalStateException();
        pool2.release(holder);
    }

    @GenerateMicroBenchmark
    public void testTuplePool() throws Exception {
        Derp derp = pool3.checkout();
        derp.a = 100;
        derp.b = 200;
        derp.c = 300;
        if (derp.a + derp.b + derp.c == System.nanoTime()) throw new IllegalStateException();
        pool3.release(derp);
    }

    @GenerateMicroBenchmark
    public void testFastTuplePreAllocIndexedBoxing() throws Exception {
        FastTuple tuple = schema.createTuple(record2);
        tuple.indexedSet(1, 100L);
        tuple.indexedSet(2, 200);
        tuple.indexedSet(3, (short)300);
        if ((Long)tuple.indexedGet(1) + (Integer)tuple.indexedGet(2) + (Short)tuple.indexedGet(3) == System.nanoTime()) throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testFastTuplePreAllocIndexed() throws Exception {
        FastTuple tuple = schema.createTuple(record2);
        tuple.indexedSetLong(1, 100L);
        tuple.indexedSetLong(2, 200L);
        tuple.indexedSetLong(3, 300L);
        if (tuple.indexedGetLong(1) + tuple.indexedGetLong(2) + tuple.indexedGetLong(3) == System.nanoTime()) throw new IllegalStateException();
    }

    @GenerateMicroBenchmark
    public void testFastTupleStaticBinding() throws Exception {
        StaticBinding tuple = (StaticBinding)schema.createTuple(record2);
        tuple.a(100L);
        tuple.b(200);
        tuple.c((short)300);
        if (tuple.a() + tuple.b() + tuple.c() == System.nanoTime()) throw new IllegalStateException();
    }

    static class Derp {
        public long a;
        public int b;
        public short c;

        public Derp(long a, int b, short c) {
            this.a = a;
            this.b = b;
            this.c = c;
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

}


