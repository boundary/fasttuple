package com.boundary.tuple;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
