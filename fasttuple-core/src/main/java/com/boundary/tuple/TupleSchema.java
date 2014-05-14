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

        /**
         * Adds a field name and type to the schema.  Field names end up as both method names and field names
         * in the generated class, therefore they have the same restrictions on allowable characters.  Passing
         * in an illegal name will cause a CompileException during the call to build.
         *
         * @param fieldName
         * @param fieldType
         * @return
         */
        public Builder addField(String fieldName, Class fieldType) {
            fn.add(fieldName);
            ft.add(fieldType);
            return this;
        }

        /**
         * The generated FastTuple subclass will implement the passed in interface.  FastTuple's produced
         * from this schema can then be cast to the interface type, for type safe invocation of the desired methods.
         *
         * @param iface
         * @return
         */
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

        /**
         * Sets the initial size for each thread local tuple pool.  The total number
         * of tuples that will be allocated can be found by multiplying this number
         * by the number of threads that will be checking tuples out of the pool.
         *
         * @param poolSize The size to generate specified in number of tuples.
         * @return
         */
        public Builder poolOfSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        /**
         * Specifies that the tuple pool should allocate more tuples when it becomes
         * exhausted.  Otherwise, an exhausted pool will throw an IllegalStateException.
         *
         * @return
         */
        public Builder expandingPool() {
            this.createWhenExhausted = true;
            return this;
        }

        /**
         * Causes this schema to allocate its memory off of the main java heap.
         *
         * @return
         */
        public DirectTupleSchema.Builder directMemory() {
            return new DirectTupleSchema.Builder(this);
        }

        /**
         * Causes this schema to allocate its memory on heap, and fully reachable by GC.
         *
         * @return
         */
        public HeapTupleSchema.Builder heapMemory() {
            return new HeapTupleSchema.Builder(this);
        }

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

    /**
     * Allocates a new tuple, completely separate from any pooling.
     *
     * @return
     * @throws Exception
     */
    public abstract FastTuple createTuple() throws Exception;

    /**
     * Allocates an array of tuples. This method will try to ensure that tuples get allocated
     * in adjacent memory, however with the heap based allocation this is not guaranteed.
     *
     * @param size the number of tuples in the array.
     * @return
     * @throws Exception
     */
    public abstract FastTuple[] createTupleArray(int size) throws Exception;

    /**
     * Returns the tuple pool for this schema.  Each individual thread accessing this method
     * will see a different pool.
     *
     * @return
     */
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

    public ClassLoader getClassLoader() {
        return clazz.getClassLoader();
    }
}
