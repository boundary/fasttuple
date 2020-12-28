package com.nickrobison.tuple;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by cliff on 5/12/14.
 */
public class TuplePoolTest {

    @Test
    public void createPoolAndCheckoutTest() {
        assertThrows(IllegalStateException.class, () -> {
            TuplePool<Long> pool = new TuplePool<>(10, false,
                    size -> {
                        Long[] ary = new Long[size];
                        Arrays.fill(ary, 0L);
                        return ary;
                    },
                    ary -> {

                    });

            for (int i = 0; i < 10; i++) {
                Long n = pool.checkout();
                assertEquals(Long.valueOf(0L), n);
            }
            pool.checkout();
        });
    }

    @Test
    public void expandPoolTest() {
        TuplePool<Long> pool = new TuplePool<>(10, true,
                size -> {
                    Long[] ary = new Long[size];
                    Arrays.fill(ary, 0L);
                    return ary;
                },
                ary -> {

                }
        );
        for (int i = 0; i < 11; i++) {
            Long n = pool.checkout();
            assertEquals(Long.valueOf(0L), n);
        }
        assertEquals(20, pool.getSize());
    }

    @Test
    public void emptyPoolTest() {
        Long[] tuples = new Long[10];
        TuplePool<Long> pool = new TuplePool<>(10, false,
                size -> {
                    Long[] ary = new Long[size];
                    Arrays.fill(ary, 0L);
                    return ary;
                },
                ary -> {

                });
        assertThrows(IllegalStateException.class, () -> {
            for (int i = 0; i < 10; i++) {
                Long n = pool.checkout();
                tuples[i] = n;
                assertEquals(Long.valueOf(0L), n);
            }
            pool.checkout();
        });

        // Return one and try again
        pool.release(tuples[5]);
        assertEquals(0L, pool.checkout());

    }

    @Test
    void testClosedPool() {
        AtomicBoolean destroyed = new AtomicBoolean(false);
        TuplePool<Long> pool = new TuplePool<>(1, true,
                size -> {
                    Long[] ary = new Long[size];
                    Arrays.fill(ary, 0L);
                    return ary;
                },
                ary -> {

                });

        pool.checkout();
        pool.close();
        final IllegalStateException exn = assertThrows(IllegalStateException.class, pool::checkout);
        assertEquals("Pool's closed everyone out!", exn.getLocalizedMessage());
//        assertTrue(destroyed.get()); // FIXME: Destroyer is never actually called
    }

    @Test
    void testBadClass() {
        TuplePool<TypedTuple> pool = new TuplePool<>(1, true,
                TypedTuple[]::new,
                ary -> {

                });

        final IllegalStateException exn = assertThrows(IllegalStateException.class, pool::checkout);
        assertEquals("Unable to reload Tuple pool", exn.getLocalizedMessage());
    }

    @Test
    void testInitializer() throws Exception {
        DirectTupleSchema schema = TupleSchema.builder().
                addField("aByte", Byte.TYPE).
                addField("aChar", Character.TYPE).
                addField("aInt", Integer.TYPE).
                addField("aShort", Short.TYPE).
                addField("aFloat", Float.TYPE).
                addField("aLong", Long.TYPE).
                addField("aDouble", Double.TYPE).
                implementInterface(TypedTuple.class).
                directMemory().
                build();
        TuplePool<TypedTuple> pool = new TuplePool<>(10, false,
                size -> schema.createTypedTupleArray(TypedTuple.class, 10),
                ary -> {

                },
                tuple -> tuple.aInt(500));

        final TypedTuple tuple = pool.checkout();
        assertEquals(500, tuple.aInt());
    }
}
