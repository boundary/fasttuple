package com.boundary.tuple;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cliff on 5/2/14.
 */
public abstract class TupleSchema {
    protected String[] fieldNames;
    protected Class[] fieldTypes;
    protected Class iface;
    protected Class clazz;
    protected int poolSize;
    protected boolean createWhenExhausted;
    protected TuplePool<FastTuple> pool;

    public static Builder builder() {
        return new Builder();
    }

    protected TupleSchema(Builder builder) {
        this.fieldNames = builder.fn.toArray(new String[builder.fn.size()]);
        this.fieldTypes = builder.ft.toArray(new Class[builder.ft.size()]);
        Preconditions.checkArgument(fieldNames.length == fieldTypes.length,
                "fieldNames and fieldTypes must have equal length");
        for (int i = 0; i < fieldNames.length; i++) {
            Preconditions.checkArgument(fieldTypes[i].isPrimitive() && !fieldTypes[i].equals(Boolean.TYPE));
        }
        this.iface = builder.iface;
        if (iface != null) {
            Preconditions.checkArgument(iface.isInterface(),
                    iface.getName() +  " is not an interface");
        }
        this.poolSize = builder.poolSize;
    }

    public static class Builder {
        protected List<String> fn;
        protected List<Class> ft;
        protected Class iface;
        protected int poolSize;
        protected int threads;
        protected boolean createWhenExhausted = false;

        public Builder(Builder builder) {
            fn = new ArrayList<>(builder.fn);
            ft = new ArrayList<>(builder.ft);
            iface = builder.iface;
            poolSize = builder.poolSize;
            threads = builder.threads;
            createWhenExhausted = builder.createWhenExhausted;
        }

        public Builder() {
            fn = Lists.newArrayList();
            ft = Lists.newArrayList();
            iface = null;
            poolSize = 0;
            threads = 0;
        }

        public Builder addField(String fieldName, Class fieldType) {
            fn.add(fieldName);
            ft.add(fieldType);
            return this;
        }

        public Builder implementInterface(Class iface) {
            this.iface = iface;
            return this;
        }

        public Builder addFieldNames(String... fieldNames) {
            for (String st : fieldNames) {
                fn.add(st);
            }
            return this;
        }

        public Builder addFieldNames(Iterable<String> fieldNames) {
            for (String st : fieldNames) {
                fn.add(st);
            }
            return this;
        }

        public Builder addField(String fieldName) {
            fn.add(fieldName);
            return this;
        }

        public Builder addFieldTypes(Class... fieldTypes) {
            for (Class c : fieldTypes) {
                ft.add(c);
            }
            return this;
        }

        public Builder addFieldTypes(Iterable<Class> fieldTypes) {
            for (Class c : fieldTypes) {
                ft.add(c);
            }
            return this;
        }

        public Builder addField(Class c) {
            ft.add(c);
            return this;
        }

        public Builder poolOfSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public Builder expandingPool() {
            this.createWhenExhausted = true;
            return this;
        }

        public DirectTupleSchema.Builder directMemory() {
            return new DirectTupleSchema.Builder(this);
        }

        public HeapTupleSchema.Builder heapMemory() { return new HeapTupleSchema.Builder(this); }

    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("(");
        for (int i=0; i<fieldNames.length; i++) {
            str.append("'");
            str.append(fieldNames[i]);
            str.append("':");
            str.append(fieldTypes[i].getName());
            if (i < fieldNames.length - 1) {
                str.append(",");
            }
        }
        str.append(")");
        return str.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TupleSchema) {
            TupleSchema o = (TupleSchema) other;
            return Arrays.equals(fieldNames, o.fieldNames) &&
                    Arrays.equals(fieldTypes, o.fieldTypes);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[] {fieldNames, fieldTypes});
    }

    public Class tupleClass() {
        return clazz;
    }

    public String[] getFieldNames() {
        return fieldNames.clone();
    }

    public Class[] getFieldTypes() {
        return fieldTypes.clone();
    }

    protected abstract void generateClass() throws Exception;

    public abstract FastTuple createTuple() throws Exception;

    public abstract FastTuple[] createTupleArray(int size) throws Exception;

    public TuplePool<FastTuple> pool() {
        return pool;
    }

    protected void generatePool() throws Exception {
        this.pool = new TuplePool<>(poolSize, createWhenExhausted, new Function<Integer, FastTuple[]>() {
            @Override
            public FastTuple[] apply(Integer integer) {
                try {
                    return createTupleArray(integer);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
