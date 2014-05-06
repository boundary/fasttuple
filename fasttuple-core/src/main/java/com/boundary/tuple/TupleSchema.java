package com.boundary.tuple;

import com.boundary.tuple.codegen.DirectTupleCodeGenerator;
import com.boundary.tuple.unsafe.Coterie;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import sun.misc.Unsafe;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.boundary.tuple.SizeOf.sizeOf;

/**
 * Created by cliff on 5/2/14.
 */
public class TupleSchema {
    private String[] fieldNames;
    private Class[] fieldTypes;
    private Class iface;
    private boolean padToCacheLine;
    // layout is the mapping from the given logical index to an offset in the tuple
    private int[] layout;
    private int[] widths;
    private int byteSize;
    private Class clazz;
    private long addressOffset;

    private static Unsafe unsafe = Coterie.unsafe();

    public static Builder builder() {
        return new Builder();
    }

    public TupleSchema(String[] fieldNames, Class[] fieldTypes, Class iface, boolean padToCacheLine) {
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
        this.layout = new int[size];
        this.widths = new int[size];
        this.padToCacheLine = padToCacheLine;
        System.arraycopy(fieldNames, 0, this.fieldNames, 0, fieldNames.length);
        System.arraycopy(fieldTypes, 0, this.fieldTypes, 0, fieldTypes.length);
        generateLayout();
    }

    public static class Builder {
        private List<String> fn;
        private List<Class> ft;
        private Class iface;
        private boolean padding;

        public Builder() {
            fn = Lists.newArrayList();
            ft = Lists.newArrayList();
            iface = null;
            padding = false;
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

        public Builder padToMachineWord(boolean padding) {
            this.padding = true;
            return this;
        }

        public TupleSchema build() {
            return new TupleSchema(
                    fn.toArray(new String[fn.size()]),
                    ft.toArray(new Class[ft.size()]),
                    iface,
                    padding
            );
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

    protected void generateLayout() {
        Member[] members = new Member[fieldNames.length];
        for (int i = 0; i < members.length; i++) {
            members[i] = new Member(i, sizeOf(fieldTypes[i]));
        }
        Arrays.sort(members, new Comparator<Member>() {
            @Override
            public int compare(Member o1, Member o2) {
                return o2.size - o1.size;
            }
        });
        int offset = 0;
        for (int i = 0; i < members.length; i++) {
            Member m = members[i];
            layout[m.index] = offset;
            widths[m.index] = m.size;
            offset += m.size;
        }
        int padding;
        if (padToCacheLine) {
            padding = 64 - (offset % 64);
        } else {
            padding = 8 - (offset % 8);
        }

        byteSize = offset + padding;
    }

    private static class Member {
        public int index;
        public int size;

        public Member(int index, int size) {
            this.index = index;
            this.size = size;
        }
    }

    public long createRecord() {
        long address = unsafe.allocateMemory(byteSize);
        unsafe.setMemory(address, byteSize, (byte) 0);
        return address;
    }

    public long createRecordArray(long size) {
        long address = unsafe.allocateMemory(size * byteSize);
        unsafe.setMemory(address, byteSize * size, (byte)0);
//        return address;
        throw new IllegalArgumentException();
    }


    public void destroy(long address) {
        unsafe.freeMemory(address);
    }

    protected void generateSchemaAccessor() throws Exception {
        if (this.clazz == null) {
            this.clazz = new DirectTupleCodeGenerator(iface, fieldNames, fieldTypes, layout).cookToClass();
            this.addressOffset = unsafe.objectFieldOffset(clazz.getField("address"));
        }
    }

    public FastTuple createTuple() throws Exception {
        long address = createRecord();
        generateSchemaAccessor();
        FastTuple tuple = (FastTuple) unsafe.allocateInstance(clazz);
        unsafe.putLong(tuple, addressOffset, address);
        return tuple;
    }

    public Class tupleClass() {
        return clazz;
    }

    public long getLong(long address, int index) {
        return unsafe.getLong(address + layout[index]);
    }

    public int getInt(long address, int index) {
        return unsafe.getInt(address + layout[index]);
    }

    public short getShort(long address, int index) {
        return unsafe.getShort(address + layout[index]);
    }

    public char getChar(long address, int index) {
        return unsafe.getChar(address + layout[index]);
    }

    public byte getByte(long address, int index) {
        return unsafe.getByte(address + layout[index]);
    }

    public double getDouble(long address, int index) {
        return unsafe.getDouble(address + layout[index]);
    }

    public float getFloat(long address, int index) {
        return unsafe.getFloat(address + layout[index]);
    }

    public void setLong(long address, int index, long value) {
        unsafe.putLong(address + layout[index], value);
    }

    public void setInt(long address, int index, int value) {
        unsafe.putInt(address + layout[index], value);
    }

    public void setShort(long address, int index, short value) {
        unsafe.putShort(address + layout[index], value);
    }

    public void setChar(long address, int index, char value) {
        unsafe.putChar(address + layout[index], value);
    }

    public void setByte(long address, int index, byte value) {
        unsafe.putByte(address + layout[index], value);
    }

    public void setFloat(long address, int index, float value) {
        unsafe.putFloat(address + layout[index], value);
    }

    public void setDouble(long address, int index, double value) {
        unsafe.putDouble(address + layout[index], value);
    }

    public int[] getLayout() {
        return layout.clone();
    }

    public int getByteSize() {
        return byteSize;
    }

    public String[] getFieldNames() {
        return fieldNames.clone();
    }

    public Class[] getFieldTypes() {
        return fieldTypes.clone();
    }
}
