package com.boundary.tuple;

/**
 * Created by cliff on 5/15/14.
 */
public interface Loader<T> {
    public T[] createArray(int size) throws Exception;
}
