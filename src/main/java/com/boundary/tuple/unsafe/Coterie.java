package com.boundary.tuple.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Created by cliff on 5/2/14.
 */
public class Coterie {
    private static Unsafe theUnsafe;

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

    public static int bitness() {
        String bitness = System.getProperty("sun.arch.data.model");
        if (bitness == null) return 32;
        if (bitness.equals("unknown")) return 32;
        return Integer.parseInt(bitness);
    }
}
