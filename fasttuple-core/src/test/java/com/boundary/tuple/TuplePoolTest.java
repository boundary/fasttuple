package com.boundary.tuple;

import com.google.common.base.Function;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * Created by cliff on 5/12/14.
 */
@RunWith(JUnit4.class)
public class TuplePoolTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void createPoolAndCheckoutTest() throws Exception {
        TuplePool<Long> pool = new TuplePool<>(10, false, new Function<Integer, Long[]>() {
            @Override
            public Long[] apply(Integer integer) {
                Long[] ary = new Long[integer];
                for (int i=0; i<ary.length; i++) {
                    ary[i] = 0L;
                }
                return ary;
            }
        });

        for (int i=0; i<10; i++) {
            Long n = pool.checkout();
            assertEquals(new Long(0L), n);
        }
        exception.expect(IllegalStateException.class);
        pool.checkout();
    }

    @Test
    public void expandPoolTest() throws Exception {
        TuplePool<Long> pool = new TuplePool<>(10, true, new Function<Integer, Long[]>() {
            @Override
            public Long[] apply(Integer integer) {
                Long[] ary = new Long[integer];
                for (int i=0; i<ary.length; i++) {
                    ary[i] = 0L;
                }
                return ary;
            }
        });
        for (int i=0; i<11; i++) {
            Long n = pool.checkout();
            assertEquals(new Long(0L), n);
        }
        assertEquals(20, pool.getSize());
    }
}
