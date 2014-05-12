package com.boundary.tuple;

/**
 * Created by cliff on 5/2/14.
 */
public abstract class FastTuple {

    /**
     * Does a boxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     */
    public abstract Object indexedGet(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not a long.
     */
    public abstract long   indexedGetLong(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not an int.
     */
    public abstract int    indexedGetInt(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not a short.
     */
    public abstract short  indexedGetShort(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not a char.
     */
    public abstract char   indexedGetChar(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not a byte.
     */
    public abstract byte   indexedGetByte(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not a float.
     */
    public abstract float  indexedGetFloat(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not a double.
     */
    public abstract double indexedGetDouble(int i);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not a long.
     */
    public abstract void   indexedSetLong(int i, long value);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not an int.
     */
    public abstract void   indexedSetInt(int i, int value);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not a short.
     */
    public abstract void   indexedSetShort(int i, short value);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not a char.
     */
    public abstract void   indexedSetChar(int i, char value);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not a byte.
     */
    public abstract void   indexedSetByte(int i, byte value);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not a float.
     */
    public abstract void   indexedSetFloat(int i, float value);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not a double.
     */
    public abstract void   indexedSetDouble(int i, double value);

    /**
     * Does a boxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the
     */
    public abstract void   indexedSet(int i, Object value);
}
