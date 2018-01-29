package com.nickrobison.tuple;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by nickrobison on 1/29/18.
 */
public class HeapTupleSchemaTest {

    public HeapTupleSchemaTest() {}

    @Test
    public void createTupleArrayTest() throws Exception {
        HeapTupleSchema schema = TupleSchema.builder().
                addField("aByte", Byte.TYPE).
                addField("aChar", Character.TYPE).
                addField("aInt", Integer.TYPE).
                addField("aShort", Short.TYPE).
                addField("aFloat", Float.TYPE).
                addField("aLong", Long.TYPE).
                addField("aDouble", Double.TYPE).
                heapMemory().
                build();

        FastTuple[] tuples = schema.createTupleArray(10);
        assertEquals(10, tuples.length);

        for (int i=0; i < 10; i++) {
            assertNotNull(tuples[i]);

            tuples[i].setByte(1, (byte) 1);
            tuples[i].setChar(2, 'b');
            tuples[i].setInt(3, 4);
            tuples[i].setShort(4, (short) 6);
            tuples[i].setFloat(5, 0.125f);
            tuples[i].setLong(6, 1000000l);
            tuples[i].setDouble(7, 0.125);

            assertEquals(1, tuples[i].getByte(1));
            assertEquals('b', tuples[i].getChar(2));
            assertEquals(4, tuples[i].getInt(3));
            assertEquals(6, tuples[i].getShort(4));
            assertEquals(0.125f, tuples[i].getFloat(5), 0.001);
            assertEquals(1000000l, tuples[i].getLong(6));
            assertEquals(0.125, tuples[i].getDouble(7), 0.001);
        }
    }

    @Test
    public void createTypedTupleArrayTest() throws Exception {
        HeapTupleSchema schema = TupleSchema.builder().
                addField("aByte", Byte.TYPE).
                addField("aChar", Character.TYPE).
                addField("aInt", Integer.TYPE).
                addField("aShort", Short.TYPE).
                addField("aFloat", Float.TYPE).
                addField("aLong", Long.TYPE).
                addField("aDouble", Double.TYPE).
                implementInterface(TypedTuple.class).
                heapMemory().
                build();

        TypedTuple[] tuples = schema.createTypedTupleArray(TypedTuple.class,10);
        assertEquals(10, tuples.length);

        for (int i=0; i < 10; i++) {
            assertNotNull(tuples[i]);

            tuples[i].aByte((byte) 1);
            tuples[i].aChar('b');
            tuples[i].aInt(4);
            tuples[i].aShort((short) 6);
            tuples[i].aFloat(0.125f);
            tuples[i].aLong(1000000l);
            tuples[i].aDouble(0.125);

            assertEquals(1, tuples[i].aByte());
            assertEquals('b', tuples[i].aChar());
            assertEquals(4, tuples[i].aInt());
            assertEquals(6, tuples[i].aShort());
            assertEquals(0.125f, tuples[i].aFloat(), 0.001);
            assertEquals(1000000l, tuples[i].aLong());
            assertEquals(0.125, tuples[i].aDouble(), 0.001);
        }
    }
}
