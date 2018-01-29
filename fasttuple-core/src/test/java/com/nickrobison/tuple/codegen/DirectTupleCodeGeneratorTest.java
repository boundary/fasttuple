package com.nickrobison.tuple.codegen;

import com.nickrobison.tuple.DirectTupleSchema;
import com.nickrobison.tuple.FastTuple;
import com.nickrobison.tuple.TupleSchema;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by cliff on 5/5/14.
 */
public class DirectTupleCodeGeneratorTest {

    @Test
    public void testAccessorsGetGenerated() throws Exception {
        DirectTupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                addField("b", Integer.TYPE).
                addField("c", Short.TYPE).
                addField("d", Character.TYPE).
                addField("e", Byte.TYPE).
                addField("f", Float.TYPE).
                addField("g", Double.TYPE).
                directMemory().
                build();

        DirectTupleCodeGenerator codegen = new DirectTupleCodeGenerator(null, schema.getFieldNames(), schema.getFieldTypes(), schema.getLayout());
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
                directMemory().
                build();
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
                directMemory().
                build();
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
                directMemory().
                build();
        FastTuple tuple = schema.createTuple();
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 1, 100L);
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 2, 40);
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 3, (short) 10);
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 4, 'j');
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 5, (byte) 255);
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 6, 0.125f);
        assertIndexedTypedGetterAndSetterRoundTrip(tuple, 7, 0.125);
    }

    @Test
    public void testInterfaceIsImplemented() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                implementInterface(StaticBinding.class).
                directMemory().
                build();
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
        tuple.set(index, value);
        assertEquals(value, tuple.get(index));
    }

    public void assertIndexedTypedGetterAndSetterRoundTrip(FastTuple tuple, int index, Object value) {
        if (value.getClass().equals(Long.class)) {
            tuple.setLong(index, (Long) value);
            assertEquals(value, tuple.getLong(index));
        } else if (value.getClass().equals(Short.class)) {
            tuple.setShort(index, (Short) value);
            assertEquals(value, tuple.getShort(index));
        } else if (value.getClass().equals(Character.class)) {
            tuple.setChar(index, (Character) value);
            assertEquals(value, tuple.getChar(index));
        } else if (value.getClass().equals(Integer.class)) {
            tuple.setInt(index, (Integer) value);
            assertEquals(value, tuple.getInt(index));
        } else if (value.getClass().equals(Byte.class)) {
            tuple.setByte(index, (Byte) value);
            assertEquals(value, tuple.getByte(index));
        } else if (value.getClass().equals(Float.class)) {
            tuple.setFloat(index, (Float) value);
            assertEquals(value, tuple.getFloat(index));
        } else if (value.getClass().equals(Double.class)) {
            tuple.setDouble(index, (Double) value);
            assertEquals(value, tuple.getDouble(index));
        }
    }

    public static interface StaticBinding {
        public void a(long a);
        public long a();
    }
}


