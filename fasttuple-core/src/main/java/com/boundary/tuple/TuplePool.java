package com.boundary.tuple;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import java.util.ArrayDeque;

/**
 * Created by cliff on 5/4/14.
 */
public class TuplePool<T> {
    private ThreadLocal<ArrayDeque<T>> pool;
    private Optional<Function<T,Void>> initializer;
    private Function<Integer,T[]> loader;
    private int size;
    private boolean createWhenExhausted;

    public TuplePool(int size, boolean createWhenExhausted, Function<Integer,T[]> loader) {
        this(size, createWhenExhausted, loader, Optional.<Function<T,Void>>absent());
    }

    public TuplePool(final int size, boolean createWhenExhausted, final Function<Integer,T[]> loader, Optional<Function<T,Void>> initializer) {
        this.size = size;
        this.initializer = initializer;
        this.createWhenExhausted = createWhenExhausted;
        this.loader = loader;
        this.pool = new ThreadLocal<ArrayDeque<T>>() {
            @Override
            protected ArrayDeque<T> initialValue() {
                ArrayDeque<T> deque = new ArrayDeque<>(size);
                reload(deque);
                return deque;
            }
        };
    }

    public T checkout() {
        ArrayDeque<T> deque = pool.get();
        if (deque.isEmpty()) {
            if (createWhenExhausted) {
                reload(deque);
            } else {
                throw new IllegalStateException("Tuple pool is exhausted.");
            }
        }
        T obj = deque.pop();
        if (initializer.isPresent()) {
            initializer.get().apply(obj);
        }
        return obj;
    }

    public void release(T obj) {
        pool.get().push(obj);
    }

    protected void reload(ArrayDeque<T> deque) {
        for (T tuple : loader.apply(size)) {
            deque.push(tuple);
        }
    }
}
