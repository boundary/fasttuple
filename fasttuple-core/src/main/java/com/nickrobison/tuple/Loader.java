package com.nickrobison.tuple;

/**
 * Created by cliff on 5/15/14.
 */
@FunctionalInterface
public interface Loader<T> {
    T[] createArray(int size) throws Exception;
}
