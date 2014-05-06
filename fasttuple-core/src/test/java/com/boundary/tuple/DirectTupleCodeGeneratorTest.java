package com.boundary.tuple;

import com.boundary.tuple.codegen.DirectTupleCodeGenerator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by cliff on 5/5/14.
 */
public class DirectTupleCodeGeneratorTest {

    @Test
    public void testAccessorsGetGenerated() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                addField("b", Integer.TYPE).
                addField("c", Short.TYPE).
                addField("d", Character.TYPE).
                addField("e", Byte.TYPE).
                addField("f", Float.TYPE).
                addField("g", Double.TYPE).
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
        
    }

    public void assertGetterAndSetterGenerated(Class clazz, String name, Class type) throws Exception {
        assertEquals(type, clazz.getDeclaredMethod(name).getReturnType());
        assertNotNull(clazz.getDeclaredMethod(name, type));
    }

    public void assertGetterAndSetterRoundTrip(Object tuple, Class clazz, String name, Class type, Object value) throws Exception {
        clazz.getDeclaredMethod(name, type).invoke(tuple, value);
        assertEquals(value, clazz.getDeclaredMethod(name).invoke(tuple));
    }
}
