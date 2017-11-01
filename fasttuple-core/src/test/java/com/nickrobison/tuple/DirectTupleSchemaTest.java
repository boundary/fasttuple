package com.nickrobison.tuple;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by cliff on 5/4/14.
 */
public class DirectTupleSchemaTest {

    public DirectTupleSchemaTest() {}

    @Test
    public void layoutTest() throws Exception {
        DirectTupleSchema schema = TupleSchema.builder().
                addField("aByte", Byte.TYPE).
                addField("aChar", Character.TYPE).
                addField("aInt", Integer.TYPE).
                addField("aShort", Short.TYPE).
                addField("aFloat", Float.TYPE).
                addField("aLong", Long.TYPE).
                addField("aDouble", Double.TYPE).
                directMemory().
                build();

        //layout should be aLong, aDouble, aInt, aFloat, aChar, aShort, aByte

        int[] layout = schema.getLayout();
        assertEquals(0, layout[5]);
        assertEquals(8, layout[6]);
        assertEquals(16, layout[2]);
        assertEquals(20, layout[4]);
        assertEquals(24, layout[1]);
        assertEquals(26, layout[3]);
        assertEquals(28, layout[0]);

        //default pad to long word
        assertEquals(32, schema.getByteSize());
    }

    @Test
    public void paddingTest() throws Exception {
        DirectTupleSchema schema = TupleSchema.builder().
                addField("aByte", Byte.TYPE).
                addField("aChar", Character.TYPE).
                addField("aInt", Integer.TYPE).
                addField("aShort", Short.TYPE).
                addField("aFloat", Float.TYPE).
                addField("aLong", Long.TYPE).
                addField("aDouble", Double.TYPE).
                directMemory().
                padToWordSize(64).
                build();

        //layout should be aLong, aDouble, aInt, aFloat, aChar, aShort, aByte
        //default pad to long word
        assertEquals(64, schema.getByteSize());
    }

    @Test
    public void createRecordTest() throws Exception {
        DirectTupleSchema schema = TupleSchema.builder().
                addField("aByte", Byte.TYPE).
                addField("aChar", Character.TYPE).
                addField("aInt", Integer.TYPE).
                addField("aShort", Short.TYPE).
                addField("aFloat", Float.TYPE).
                addField("aLong", Long.TYPE).
                addField("aDouble", Double.TYPE).
                directMemory().
                build();

        long record = schema.createRecord();
        schema.setByte(record,   0, (byte)6);
        schema.setChar(record,   1, (char)8);
        schema.setInt(record,    2, 50010);
        schema.setShort(record,  3, (short)2500);
        schema.setFloat(record,  4, 0.1f);
        schema.setLong(record,   5, 100000l);
        schema.setDouble(record, 6, 0.59403);

        assertEquals(6, schema.getByte(record, 0));
        assertEquals(8, schema.getChar(record, 1));
        assertEquals(50010, schema.getInt(record, 2));
        assertEquals(2500, schema.getShort(record, 3));
        assertEquals(0.1, schema.getFloat(record, 4), 0.00001);
        assertEquals(100000l, schema.getLong(record, 5));
        assertEquals(0.59403, schema.getDouble(record, 6), 0.00001);
    }

    @Test
    public void createTupleArrayTest() throws Exception {
        DirectTupleSchema schema = TupleSchema.builder().
                addField("aByte", Byte.TYPE).
                addField("aChar", Character.TYPE).
                addField("aInt", Integer.TYPE).
                addField("aShort", Short.TYPE).
                addField("aFloat", Float.TYPE).
                addField("aLong", Long.TYPE).
                addField("aDouble", Double.TYPE).
                directMemory().
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
}
