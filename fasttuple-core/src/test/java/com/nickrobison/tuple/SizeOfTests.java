package com.nickrobison.tuple;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by nickrobison on 12/28/20.
 * <p>
 * Helper test to get PITest passing, really not very exciting.
 */
public class SizeOfTests {

    @Test
    void testSizeOf() {
        assertEquals(1, SizeOf.sizeOf(Byte.TYPE));
    }
}
