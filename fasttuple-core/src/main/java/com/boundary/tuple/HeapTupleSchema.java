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
            this.fn = builder.fn;
            this.ft = builder.ft;
            this.iface = builder.iface;
        }

        public HeapTupleSchema build() {
            return new HeapTupleSchema(fn.toArray(new String[fn.size()]), ft.toArray(new Class[ft.size()]), iface);
        }
    }

    public HeapTupleSchema(String[] fieldNames, Class[] fieldTypes, Class iface) {
        super(fieldNames, fieldTypes, iface);
    }

    @Override
    public void generateClass() throws Exception {
        this.clazz = new HeapTupleCodeGenerator(iface, fieldNames, fieldTypes).cookToClass();
        this.cons = clazz.getConstructor();
    }

    @Override
    public FastTuple createTuple() throws Exception {
        return (FastTuple)cons.newInstance();
    }
}
