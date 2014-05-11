package com.boundary.tuple;

import com.boundary.tuple.codegen.HeapTupleCodeGenerator;

import java.lang.reflect.Constructor;

/**
 * Created by cliff on 5/9/14.
 */
public class HeapTupleSchema extends TupleSchema {
    protected Constructor cons;

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
        generatePool();
    }

    @Override
    protected void generateClass() throws Exception {
        this.clazz = new HeapTupleCodeGenerator(iface, fieldNames, fieldTypes).cookToClass();
        this.cons = clazz.getConstructor();
    }

    @Override
    public FastTuple createTuple() throws Exception {
        return (FastTuple)cons.newInstance();
    }

    @Override
    public FastTuple[] createTupleArray(int size) throws Exception {
        FastTuple[] tuples = new FastTuple[size];
        for (int i=0; i<size; i++) {
            tuples[i] = createTuple();
        }
        return tuples;
    }
}
