package com.boundary.tuple;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

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

    public static Builder builder() {
        return new Builder();
    }

    public TupleSchema(String[] fieldNames, Class[] fieldTypes, Class iface) {
        Preconditions.checkArgument(fieldNames.length == fieldTypes.length,
                "fieldNames and fieldTypes must have equal length");
        for (int i = 0; i < fieldNames.length; i++) {
            Preconditions.checkArgument(fieldTypes[i].isPrimitive() && !fieldTypes[i].equals(Boolean.TYPE));
        }
        if (iface != null) {
            Preconditions.checkArgument(iface.isInterface(),
                    iface.getName() +  " is not an interface");
        }
        int size = fieldNames.length;
        this.fieldNames = new String[size];
        this.fieldTypes = new Class[size];

        this.iface = iface;
        System.arraycopy(fieldNames, 0, this.fieldNames, 0, fieldNames.length);
        System.arraycopy(fieldTypes, 0, this.fieldTypes, 0, fieldTypes.length);
    }

    public static class Builder {
        protected List<String> fn;
        protected List<Class> ft;
        protected Class iface;

        public Builder() {
            fn = Lists.newArrayList();
            ft = Lists.newArrayList();
            iface = null;
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

        public DirectTupleSchema.Builder directMemory() {
            return new DirectTupleSchema.Builder(this);
        }

//        public HeapTupleSchema.Builder heapMemory() { return new HeapTupleSchema.Builder(this); }

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

    public abstract void generateClass() throws Exception;

    public abstract FastTuple createTuple() throws Exception;
}
