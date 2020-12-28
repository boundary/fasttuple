package com.nickrobison.tuple;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by nickrobison on 12/28/20.
 */
public class SchemaBuilderTests {

    @Test
    void testNonInterface() {

        final List<String> nameList = Arrays.asList("a", "b", "c");
        final List<Class> typeList = Arrays.asList(Long.TYPE, Long.TYPE, Long.TYPE);
        final IllegalArgumentException exn = assertThrows(IllegalArgumentException.class, () -> TupleSchema.builder().
                addFieldNames(nameList).
                addFieldTypes(typeList).
                implementInterface(String.class).
                directMemory().
                build());

        assertEquals("java.lang.String is not an interface", exn.getLocalizedMessage());
    }

    @Test
    void testUnevenNameFields() {
        final IllegalArgumentException exn = assertThrows(IllegalArgumentException.class, () -> TupleSchema.builder()
                .addFieldNames("Test", "Field")
                .addFieldTypes(String.class).
                        heapMemory().
                        build());

        assertEquals("fieldNames and fieldTypes must have equal length", exn.getLocalizedMessage());
    }

    @Test
    void testNonPrimitiveFieldTypes() {
        assertThrows(IllegalArgumentException.class, () -> TupleSchema.builder()
                .addFieldNames("Test", "Field")
                .addFieldTypes(String.class, Integer.class).
                        heapMemory().
                        build());

        assertThrows(IllegalArgumentException.class, () -> TupleSchema.builder()
                .addFieldNames("Test", "Field")
                .addFieldTypes(Boolean.class, Integer.class).
                        heapMemory().
                        build());
    }

    @Test
    void testBuilderPool() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                addField("b", Long.TYPE).
                addField("c", Long.TYPE).
                poolOfSize(10).
                expandingPool().
                heapMemory().
                build();

        // Pool based allocation isn't implemented yet.
        assertEquals(0, schema.pool().getSize());
    }

    @Test
    void testHelperMethods() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                addField("b", Long.TYPE).
                addField("c", Long.TYPE).
                heapMemory().
                build();

        TupleSchema s2 = TupleSchema.builder().
                addField("a", Long.TYPE).
                addField("b", Long.TYPE).
                addField("d", Integer.TYPE).
                heapMemory().
                build();
        assertEquals(schema, schema);
        //noinspection RedundantCast - Suppress this because we need to test the equals method
        assertNotEquals((Object) schema, (Object) "I'm a string");
        assertNotEquals(schema, s2);
        assertEquals("('a':long,'b':long,'c':long)", schema.toString());
        assertEquals(schema.hashCode(), schema.hashCode());
        assertNotEquals(schema.hashCode(), s2.hashCode());
    }
}
