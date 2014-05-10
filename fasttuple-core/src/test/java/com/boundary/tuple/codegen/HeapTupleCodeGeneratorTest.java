package com.boundary.tuple.codegen;

import com.boundary.tuple.FastTuple;
import com.boundary.tuple.HeapTupleSchema;
import com.boundary.tuple.TupleSchema;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by cliff on 5/9/14.
 */
public class HeapTupleCodeGeneratorTest {
    @Test
    public void testAccessorsGetGenerated() throws Exception {
        HeapTupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                addField("b", Integer.TYPE).
                addField("c", Short.TYPE).
                addField("d", Character.TYPE).
                addField("e", Byte.TYPE).
                addField("f", Float.TYPE).
                addField("g", Double.TYPE).
                heapMemory().
                build();

        HeapTupleCodeGenerator codegen = new HeapTupleCodeGenerator(null, schema.getFieldNames(), schema.getFieldTypes());
        Class clazz = codegen.cookToClass();
        assertGetterAndSetterGenerated(clazz, "a", long.class);
        assertGetterAndSetterGenerated(clazz, "b", int.class);
        assertGetterAndSetterGenerated(clazz, "c", short.class);
        assertGetterAndSetterGenerated(clazz, "d", char.class);
        assertGetterAndSetterGenerated(clazz, "e", byte.class);
        assertGetterAndSetterGenerated(clazz, "f", float.class);
        assertGetterAndSetterGenerated(clazz, "g", double.class);
    }

    @Test
    public void testAccessorsWork() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                addField("b", Integer.TYPE).
                addField("c", Short.TYPE).
                addField("d", Character.TYPE).
                addField("e", Byte.TYPE).
                addField("f", Float.TYPE).
                addField("g", Double.TYPE).
                heapMemory().
                build();
        schema.generateClass();
        FastTuple tuple = schema.createTuple();
        assertGetterAndSetterRoundTrip(tuple, schema.tupleClass(), "a", Long.TYPE, 100L);
        assertGetterAndSetterRoundTrip(tuple, schema.tupleClass(), "b", Integer.TYPE, 40);
        assertGetterAndSetterRoundTrip(tuple, schema.tupleClass(), "c", Short.TYPE, (short)10);
        assertGetterAndSetterRoundTrip(tuple, schema.tupleClass(), "d", Character.TYPE, 'j');
        assertGetterAndSetterRoundTrip(tuple, schema.tupleClass(), "e", Byte.TYPE, (byte)255);
        assertGetterAndSetterRoundTrip(tuple, schema.tupleClass(), "f", Float.TYPE, 0.125f);
        assertGetterAndSetterRoundTrip(tuple, schema.tupleClass(), "g", Double.TYPE, 0.125);
    }

    @Test
    public void testIndexedSetAndGet() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                addField("b", Integer.TYPE).
                addField("c", Short.TYPE).
                addField("d", Character.TYPE).
                addField("e", Byte.TYPE).
                addField("f", Float.TYPE).
                addField("g", Double.TYPE).
                heapMemory().
                build();
        schema.generateClass();
        FastTuple tuple = schema.createTuple();
        assertIndexedGetterAndSetterRoundTrip(tuple, 1, 100L);
        assertIndexedGetterAndSetterRoundTrip(tuple, 2, 40);
        assertIndexedGetterAndSetterRoundTrip(tuple, 3, (short)10);
        assertIndexedGetterAndSetterRoundTrip(tuple, 4, 'j');
        assertIndexedGetterAndSetterRoundTrip(tuple, 5, (byte)255);
        assertIndexedGetterAndSetterRoundTrip(tuple, 6, 0.125f);
        assertIndexedGetterAndSetterRoundTrip(tuple, 7, 0.125);
    }

    @Test
    public void testIndexedTypedSetAndGet() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                addField("b", Integer.TYPE).
                addField("c", Short.TYPE).
                addField("d", Character.TYPE).
                addField("e", Byte.TYPE).
                addField("f", Float.TYPE).
                addField("g", Double.TYPE).
                heapMemory().
                build();
        schema.generateClass();
        FastTuple tuple = schema.createTuple();
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 1, 100L);
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 2, 40);
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 3, (short)10);
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 4, 'j');
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 5, (byte)255);
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 6, 0.125f);
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 7, 0.125);
    }

    @Test
    public void testInterfaceIsImplemented() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                implementInterface(StaticBinding.class).
                heapMemory().
                build();
        schema.generateClass();
        FastTuple tuple = schema.createTuple();
        assertTrue(tuple instanceof StaticBinding);
    }

    public void assertGetterAndSetterGenerated(Class clazz, String name, Class type) throws Exception {
        assertEquals(type, clazz.getDeclaredMethod(name).getReturnType());
        assertNotNull(clazz.getDeclaredMethod(name, type));
    }

    public void assertGetterAndSetterRoundTrip(Object tuple, Class clazz, String name, Class type, Object value) throws Exception {
        clazz.getDeclaredMethod(name, type).invoke(tuple, value);
        assertEquals(value, clazz.getDeclaredMethod(name).invoke(tuple));
    }

    public void assertIndexedGetterAndSetterRoundTrip(FastTuple tuple, int index, Object value) {
        tuple.indexedSet(index, value);
        assertEquals(value, tuple.indexedGet(index));
    }

    public void assertIndexedTypedGetterAndSetterRoundTrip(FastTuple tuple, int index, Object value) {
        if (value.getClass().equals(Long.class)) {
            tuple.indexedSetLong(index, (Long)value);
            assertEquals(value, tuple.indexedGetLong(index));
        } else if (value.getClass().equals(Short.class)) {
            tuple.indexedSetShort(index, (Short)value);
            assertEquals(value, tuple.indexedGetShort(index));
        } else if (value.getClass().equals(Character.class)) {
            tuple.indexedSetChar(index, (Character)value);
            assertEquals(value, tuple.indexedGetChar(index));
        } else if (value.getClass().equals(Integer.class)) {
            tuple.indexedSetInt(index, (Integer)value);
            assertEquals(value, tuple.indexedGetInt(index));
        } else if (value.getClass().equals(Byte.class)) {
            tuple.indexedSetByte(index, (Byte)value);
            assertEquals(value, tuple.indexedGetByte(index));
        } else if (value.getClass().equals(Float.class)) {
            tuple.indexedSetFloat(index, (Float)value);
            assertEquals(value, tuple.indexedGetFloat(index));
        } else if (value.getClass().equals(Double.class)) {
            tuple.indexedSetDouble(index, (Double)value);
            assertEquals(value, tuple.indexedGetDouble(index));
        }
    }

    public static interface StaticBinding {
        public void a(long a);
        public long a();
    }
}
