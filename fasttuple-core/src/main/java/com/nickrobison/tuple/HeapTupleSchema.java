package com.nickrobison.tuple;

import com.nickrobison.tuple.codegen.HeapTupleCodeGenerator;
import com.nickrobison.tuple.codegen.TupleAllocatorGenerator;

import java.lang.reflect.Array;

/**
 * Created by cliff on 5/9/14.
 */
public class HeapTupleSchema extends TupleSchema {
    private TupleAllocatorGenerator.TupleAllocator allocator;

    public static class Builder extends TupleSchema.Builder {

        public Builder(TupleSchema.Builder builder) {
            super(builder);
        }

        public HeapTupleSchema build() throws Exception {
            return new HeapTupleSchema(this);
        }
    }

    public HeapTupleSchema(Builder builder) throws Exception {
        super(builder);
        generateClass();
    }

    @Override
    protected void generateClass() throws Exception {
        this.clazz = new HeapTupleCodeGenerator(iface, fieldNames, fieldTypes).cookToClass();
        this.allocator = new TupleAllocatorGenerator(clazz).createAllocator();
    }

    @Override
    public FastTuple createTuple() throws Exception {
        return allocator.allocate();
    }

    @Override
    public <T> T createTypedTuple(Class<T> clazz) throws Exception {
        return clazz.cast(allocator.allocate());
    }

    @Override
    public FastTuple[] createTupleArray(int size) throws Exception {
        FastTuple[] tuples = new FastTuple[size];
        for (int i=0; i<size; i++) {
            tuples[i] = createTuple();
        }
        return tuples;
    }

    @Override
    public <T> T[] createTypedTupleArray(Class<T> clazz, int size) throws Exception {
        // Use Array native method to create array
        // of a type only known at run time
        @SuppressWarnings("unchecked")
        T[] tuples =  (T[]) Array.newInstance(clazz, size);
        for (int i = 0; i < size; i++) {
            tuples[i] = createTypedTuple(clazz);
        }
        return tuples;
    }

    @Override
    public void destroyTuple(FastTuple tuple) {
        //noop
    }

    @Override
    public <T> void destroyTypedTuple(T tuple) {
//        noop
    }

    @Override
    public void destroyTupleArray(FastTuple[] ary) {
        //noop
    }

    @Override
    public <T> void destroyTypedTupleArray(T[] ary) {
//        noop
    }
}
