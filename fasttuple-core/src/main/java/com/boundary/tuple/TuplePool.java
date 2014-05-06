package com.boundary.tuple;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import java.util.ArrayDeque;

/**
 * Created by cliff on 5/4/14.
 */
public class TuplePool<T> {
    private ThreadLocal<ArrayDeque<T>> pool;
    private Function<Void,T> loader;
    private Optional<Function<T,Void>> initializer;
    private int size;

    public TuplePool(int size, Function<Void,T> loader) {
        this(size, loader, Optional.<Function<T,Void>>absent());
    }

    public TuplePool(final int size, final Function<Void, T> loader, Optional<Function<T,Void>> initializer) {
        this.size = size;
        this.loader = loader;
        this.initializer = initializer;
        this.pool = new ThreadLocal<ArrayDeque<T>>() {
            @Override
            protected ArrayDeque<T> initialValue() {
                return new ArrayDeque<T>(size);
            }
        };
    }

    public T checkout() {
        if (pool.get().isEmpty()) {
            return loader.apply(null);
        }
        T obj = pool.get().pop();
        if (initializer.isPresent()) {
            initializer.get().apply(obj);
        }
        return obj;
    }

    public void release(T obj) {
        pool.get().push(obj);
    }
}
