package com.nickrobison.tuple;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

                    }
            );

            for (int i = 0; i < 10; i++) {
                Long n = pool.checkout();
                assertEquals(new Long(0L), n);
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
            assertEquals(new Long(0L), n);
        }
        assertEquals(20, pool.getSize());
    }
}
