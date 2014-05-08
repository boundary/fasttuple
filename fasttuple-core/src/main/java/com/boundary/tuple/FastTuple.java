package com.boundary.tuple;

/**
 * Created by cliff on 5/2/14.
 */
public abstract class FastTuple {
    public abstract Object indexedGet(int i);
    public abstract long   indexedGetLong(int i);
    public abstract int    indexedGetInt(int i);
    public abstract short  indexedGetShort(int i);
    public abstract char   indexedGetChar(int i);
    public abstract byte   indexedGetByte(int i);
    public abstract float  indexedGetFloat(int i);
    public abstract double indexedGetDouble(int i);
    public abstract void   indexedSetLong(int i, long value);
    public abstract void   indexedSetInt(int i, int value);
    public abstract void   indexedSetShort(int i, short value);
    public abstract void   indexedSetChar(int i, char value);
    public abstract void   indexedSetByte(int i, byte value);
    public abstract void   indexedSetFloat(int i, float value);
    public abstract void   indexedSetDouble(int i, double value);
    public abstract void   indexedSet(int i, Object value);
}
