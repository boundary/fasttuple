package com.nickrobison.tuple;

/**
 * Created by cliff on 5/15/14.
 */
@FunctionalInterface
public interface Destroyer<T> {
    void destroyArray(T[] ary);
}
