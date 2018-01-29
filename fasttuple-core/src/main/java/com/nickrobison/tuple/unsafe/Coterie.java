package com.nickrobison.tuple.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Created by cliff on 5/2/14.
 */
public class Coterie {
    private static final Unsafe theUnsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            theUnsafe = (Unsafe) field.get(null);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Unsafe unsafe() {
        return theUnsafe;
    }
}
