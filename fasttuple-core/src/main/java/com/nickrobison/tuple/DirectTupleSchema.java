package com.nickrobison.tuple;

import com.nickrobison.tuple.codegen.DirectTupleCodeGenerator;
import com.nickrobison.tuple.codegen.TupleAllocatorGenerator;
import com.nickrobison.tuple.unsafe.Coterie;
import sun.misc.Unsafe;

import java.util.Arrays;
import java.util.Objects;

import static com.nickrobison.tuple.SizeOf.sizeOf;

/**
 * Created by cliff on 5/9/14.
 */
public class DirectTupleSchema extends TupleSchema {
    // layout is the mapping from the given logical index to an offset in the tuple
    protected final int[] layout;
    protected final int[] widths;
    protected int byteSize;
    protected long addressOffset;
    protected final int wordSize;
    protected TupleAllocatorGenerator.TupleAllocator allocator;

    private static final Unsafe unsafe = Coterie.unsafe();

    public static class Builder extends TupleSchema.Builder {
        protected int wordSize = 8;

        public Builder(TupleSchema.Builder builder) {
            super(builder);
        }

        /**
         * Pads out the size of each individual record such that it fits within a multiple of the wordSize.
         * This is useful for eliminating false sharing when adjacent records are being utilized by different
         * threads.
         *
         * @param wordSize - Word size to pad between tuples
         * @return - {@link Builder}
         */
        public Builder padToWordSize(int wordSize) {
            this.wordSize = wordSize;
            return this;
        }

        public DirectTupleSchema build() throws Exception {
            return new DirectTupleSchema(this);
        }
    }

    public DirectTupleSchema(Builder builder) throws Exception {
        super(builder);
        int size = fieldNames.length;
        this.layout = new int[size];
        this.widths = new int[size];
        this.wordSize = builder.wordSize;
        generateLayout();
        generateClass();
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

    public FastTuple createTuple(long address) {
        FastTuple tuple = allocator.allocate();
        unsafe.putLong(tuple, addressOffset, address);
        return tuple;
    }

    public long createRecord() {
        return unsafe.allocateMemory(byteSize);
    }

    public long createRecordArray(long size) {
        return unsafe.allocateMemory(size * byteSize);
    }

    public FastTuple createTuple() throws Exception {
        long address = createRecord();
        return createTuple(address);
    }

    @Override
    public FastTuple[] createTupleArray(int size) throws Exception {
        long address = createRecordArray(size);
        FastTuple[] tuples = new FastTuple[size];
        for (int i = 0; i < size; i++) {
            tuples[i] = createTuple(address + byteSize * i);
        }
        return tuples;
    }

    @Override
    public void destroyTuple(FastTuple tuple) {
        long address = unsafe.getLong(tuple, addressOffset);
        unsafe.freeMemory(address);
    }

    @Override
    public void destroyTupleArray(FastTuple[] ary) {
        long address = unsafe.getLong(ary[0], addressOffset);
        unsafe.freeMemory(address);
    }

    public void destroy(FastTuple tuple) {
        if (clazz.isInstance(tuple)) {
            long address = unsafe.getLong(tuple, addressOffset);
            destroy(address);
        }
    }

    public void destroy(long address) {
        unsafe.freeMemory(address);
    }

    protected void generateClass() throws Exception {
        if (this.clazz == null) {
            this.clazz = new DirectTupleCodeGenerator(iface, fieldNames, fieldTypes, layout).cookToClass();
            this.addressOffset = unsafe.objectFieldOffset(clazz.getField("address"));
            TupleAllocatorGenerator generator = new TupleAllocatorGenerator(clazz);
            this.allocator = generator.createAllocator();
        }
    }

    protected void generateLayout() {
        Member[] members = new Member[fieldNames.length];
        for (int i = 0; i < members.length; i++) {
            members[i] = new Member(i, sizeOf(fieldTypes[i]));
        }
        Arrays.sort(members, (o1, o2) -> o2.size - o1.size);
        int offset = 0;
        for (Member m : members) {
            layout[m.index] = offset;
            widths[m.index] = m.size;
            offset += m.size;
        }
        int padding = wordSize - (offset % wordSize);
        byteSize = offset + padding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DirectTupleSchema that = (DirectTupleSchema) o;
        return byteSize == that.byteSize &&
                addressOffset == that.addressOffset &&
                wordSize == that.wordSize &&
                Arrays.equals(layout, that.layout) &&
                Arrays.equals(widths, that.widths) &&
                Objects.equals(allocator, that.allocator);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(super.hashCode(), byteSize, addressOffset, wordSize, allocator);
        result = 31 * result + Arrays.hashCode(layout);
        result = 31 * result + Arrays.hashCode(widths);
        return result;
    }

    private static class Member {
        public final int index;
        public final int size;

        public Member(int index, int size) {
            this.index = index;
            this.size = size;
        }
    }
}
