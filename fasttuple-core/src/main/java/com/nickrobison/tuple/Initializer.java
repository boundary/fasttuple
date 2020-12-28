package com.nickrobison.tuple;

/**
 * Created by nickrobison on 12/28/20.
 */
@FunctionalInterface
public interface Initializer<T> {
    void initialize(T value);
}
