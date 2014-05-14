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
    private int reloadSize;
    private boolean createWhenExhausted;

    public TuplePool(int size, boolean createWhenExhausted, Function<Integer,T[]> loader) {
        this(size, createWhenExhausted, loader, Optional.<Function<T,Void>>absent());
    }

    public TuplePool(final int size, boolean createWhenExhausted, final Function<Integer,T[]> loader, Optional<Function<T,Void>> initializer) {
        this.size = 0;
        this.reloadSize = size;
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
        possiblyReload(deque);
        T obj = deque.pop();
        initialize(obj);
        return obj;
    }

    public void release(T obj) {
        pool.get().push(obj);
    }

    public int getSize() {
        return size;
    }

    private void initialize(T obj) {
        if (initializer.isPresent()) {
            initializer.get().apply(obj);
        }
    }

    private void possiblyReload(ArrayDeque<T> deque) {
        if (deque.isEmpty()) {
            if (createWhenExhausted) {
                reload(deque);
            } else {
                throw new IllegalStateException("Tuple pool is exhausted.");
            }
        }
    }

    private void reload(ArrayDeque<T> deque) {
        size += reloadSize;
        for (T tuple : loader.apply(reloadSize)) {
            deque.push(tuple);
        }
    }
}
