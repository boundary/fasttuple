package com.boundary.tuple;

/**
 * Created by cliff on 5/3/14.
 */
public final class SizeOf {

    private SizeOf() {}

    public static int sizeOf(Class c) {
        if (c.equals(Byte.TYPE)) {
            return 1;
        } else if (c.equals(Short.TYPE) || c.equals(Character.TYPE)) {
            return 2;
        } else if (c.equals(Integer.TYPE) || c.equals(Float.TYPE)) {
            return 4;
        } else {
            return 8;
        }
    }
}
