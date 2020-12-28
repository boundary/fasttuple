package com.nickrobison.tuple.codegen;

import com.nickrobison.tuple.FastTuple;
import com.nickrobison.tuple.TupleSchema;
import org.codehaus.commons.compiler.CompileException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by cliff on 5/12/14.
 */
public class TupleExpressionGeneratorTest {

    @Test
    public void testGetLongOut() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                addField("b", Long.TYPE).
                addField("c", Long.TYPE).
                heapMemory().
                build();

        FastTuple tuple = schema.createTuple();
        tuple.setLong(1, 100L);
        tuple.setLong(2, 600L);
        tuple.setLong(3, 1000L);

        TupleExpressionGenerator.LongTupleExpression eval = TupleExpressionGenerator.builder().expression("tuple.a + tuple.b + tuple.c").schema(schema).returnLong();
        assertEquals(1700L, eval.evaluate(tuple));
    }

    @Test
    public void testGetBooleanOut() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                addField("b", Long.TYPE).
                addField("c", Long.TYPE).
                heapMemory().
                build();

        FastTuple tuple = schema.createTuple();
        tuple.setLong(1, 100L);
        tuple.setLong(2, 600L);
        tuple.setLong(3, 1000L);

        TupleExpressionGenerator.BooleanTupleExpression eval = TupleExpressionGenerator.builder().expression("tuple.a == 100L").schema(schema).returnBoolean();
        assertTrue(eval.evaluate(tuple));
    }

    @Test
    public void testGetIntOut() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Integer.TYPE).
                addField("b", Integer.TYPE).
                addField("c", Integer.TYPE).
                heapMemory().
                build();

        FastTuple tuple = schema.createTuple();
        tuple.setInt(1, 100);
        tuple.setInt(2, 600);
        tuple.setInt(3, 1000);

        TupleExpressionGenerator.IntTupleExpression eval = TupleExpressionGenerator.builder().expression("tuple.a + tuple.b + tuple.c").schema(schema).returnInt();
        assertEquals(1700, eval.evaluate(tuple));
    }

    @Test
    public void testGetShortOut() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Short.TYPE).
                addField("b", Short.TYPE).
                addField("c", Short.TYPE).
                heapMemory().
                build();

        FastTuple tuple = schema.createTuple();
        tuple.setShort(1, (short) 100);
        tuple.setShort(2, (short) 10);
        tuple.setShort(3, (short) 1);

        // We have to manually cast to short, which seems silly, but not sure if its worth the fix since shorts aren't very common.
        TupleExpressionGenerator.ShortTupleExpression eval = TupleExpressionGenerator.builder().expression("(short) (tuple.a + tuple.b + tuple.c)").schema(schema).returnShort();
        assertEquals((short) 111, eval.evaluate(tuple));
    }

    @Test
    public void testGetFloatOut() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Float.TYPE).
                addField("b", Float.TYPE).
                addField("c", Float.TYPE).
                heapMemory().
                build();

        FastTuple tuple = schema.createTuple();
        tuple.setFloat(1, (float) 100.0);
        tuple.setFloat(2, (float) 10.5);
        tuple.setFloat(3, (float) 1.1);

        TupleExpressionGenerator.FloatTupleExpression eval = TupleExpressionGenerator.builder().expression("tuple.a + tuple.b + tuple.c").schema(schema).returnFloat();
        assertEquals(111.6, eval.evaluate(tuple), .001);
    }

    @Test
    public void testGetDoubleOut() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Double.TYPE).
                addField("b", Double.TYPE).
                addField("c", Double.TYPE).
                heapMemory().
                build();

        FastTuple tuple = schema.createTuple();
        tuple.setDouble(1, 100.0);
        tuple.setDouble(2, 10.5);
        tuple.setDouble(3, 1.1);

        TupleExpressionGenerator.DoubleTupleExpression eval = TupleExpressionGenerator.builder().expression("tuple.a + tuple.b + tuple.c").schema(schema).returnDouble();
        assertEquals(111.6, eval.evaluate(tuple), .001);
    }

    @Test
    public void testGetObjectOut() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Double.TYPE).
                addField("b", Double.TYPE).
                addField("c", Double.TYPE).
                heapMemory().
                build();

        FastTuple tuple = schema.createTuple();
        tuple.set(1, 100.0);
        tuple.set(2, 10.5);
        tuple.set(3, 1.1);

        TupleExpressionGenerator.ObjectTupleExpression eval = TupleExpressionGenerator.builder().expression("tuple.a + tuple.b + tuple.c").schema(schema).returnObject();
        assertEquals(111.6, (Double) eval.evaluate(tuple), .001);
    }

    @Test
    public void testGetCharOut() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Character.TYPE).
                addField("b", Character.TYPE).
                heapMemory().
                build();

        FastTuple tuple = schema.createTuple();
        tuple.setChar(1, 'h');
        tuple.setChar(2, 'w');

        TupleExpressionGenerator.CharTupleExpression eval = TupleExpressionGenerator.builder().expression("tuple.a ").schema(schema).returnChar();
        assertEquals('h', eval.evaluate(tuple));
    }

    @Test
    public void testGetByteOut() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Byte.TYPE).
                heapMemory().
                build();

        FastTuple tuple = schema.createTuple();
        tuple.setByte(1, (byte) 'h');

        TupleExpressionGenerator.ByteTupleExpression eval = TupleExpressionGenerator.builder().expression("tuple.a ").schema(schema).returnByte();
        assertEquals((byte) 'h', eval.evaluate(tuple));
    }

    @Test
    public void testMultiExpr() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Long.TYPE).
                addField("b", Long.TYPE).
                addField("c", Long.TYPE).
                heapMemory().
                build();

        FastTuple tuple = schema.createTuple();
        TupleExpressionGenerator.TupleExpression eval = TupleExpressionGenerator.builder().expression("tuple.a(100), tuple.b(200), tuple.c(300)").schema(schema).returnVoid();
        eval.evaluate(tuple);
        assertEquals(100L, tuple.getLong(1));
        assertEquals(200, tuple.getLong(2));
        assertEquals(300, tuple.getLong(3));
    }

    @Test
    public void testMalformedExpr() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Character.TYPE).
                addField("b", Character.TYPE).
                heapMemory().
                build();

        FastTuple tuple = schema.createTuple();
        tuple.setChar(1, 'h');
        tuple.setChar(2, 'w');

        assertThrows(CompileException.class, () -> TupleExpressionGenerator.builder().expression("tuple.cd").schema(schema).returnChar());
    }

    @Test
    void testNoReturnExpr() throws Exception {
        TupleSchema schema = TupleSchema.builder().
                addField("a", Double.TYPE).
                addField("b", Double.TYPE).
                addField("c", Double.TYPE).
                heapMemory().
                build();

        FastTuple tuple = schema.createTuple();
        tuple.setDouble(1, 100.0);
        tuple.setDouble(2, 10.5);
        tuple.setDouble(3, 1.1);

        assertThrows(CompileException.class, () -> TupleExpressionGenerator.builder().expression("tuple.a + tuple.b == tuple.c").schema(schema).returnDouble());
    }
}
