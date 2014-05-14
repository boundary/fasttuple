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
    public abstract Object get(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not a long.
     */
    public abstract long getLong(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not an int.
     */
    public abstract int getInt(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not a short.
     */
    public abstract short getShort(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not a char.
     */
    public abstract char getChar(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not a byte.
     */
    public abstract byte getByte(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not a float.
     */
    public abstract float getFloat(int i);

    /**
     * Does an unboxed get on a tuple field.
     *
     * @param i Index of the field to get. Counting starts at 1.
     * @return The value.
     * @throws java.lang.IllegalArgumentException if the specified field is not a double.
     */
    public abstract double getDouble(int i);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not a long.
     */
    public abstract void setLong(int i, long value);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not an int.
     */
    public abstract void setInt(int i, int value);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not a short.
     */
    public abstract void setShort(int i, short value);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not a char.
     */
    public abstract void setChar(int i, char value);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not a byte.
     */
    public abstract void setByte(int i, byte value);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not a float.
     */
    public abstract void setFloat(int i, float value);

    /**
     * Does an unboxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the specified field is not a double.
     */
    public abstract void setDouble(int i, double value);

    /**
     * Does a boxed set on a tuple field.
     *
     * @param i Index of the field to set. Counting starts at 1.
     * @param value The value to set.
     * @throws java.lang.IllegalArgumentException if the
     */
    public abstract void set(int i, Object value);
}
