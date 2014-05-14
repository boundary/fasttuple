package com.boundary.tuple;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import java.util.ArrayDeque;

/**
 * Created by cliff on 5/4/14.
 */
public class TuplePool<T> {
    private final ThreadLocal<ArrayDeque<T>> pool;
    private final Optional<Function<T,Void>> initializer;
    private final Function<Integer,T[]> loader;
    private int size;
    private final int reloadSize;
    private final boolean createWhenExhausted;

    public TuplePool(int size, boolean createWhenExhausted, Function<Integer,T[]> loader) {
        this(size, createWhenExhausted, loader, null);
    }

    public TuplePool(final int size, boolean createWhenExhausted, final Function<Integer,T[]> loader, Function<T,Void> initializer) {
        this.size = 0;
        this.reloadSize = size;
        this.createWhenExhausted = createWhenExhausted;
        this.loader = Preconditions.checkNotNull(loader);
        this.initializer = Optional.fromNullable(initializer);
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
        final T[] tuples = loader.apply(reloadSize);
        if (tuples == null) {
            throw new IllegalStateException("Unable to reload Tuple pool");
        }
        size += reloadSize;
        for (T tuple : tuples) {
            deque.push(tuple);
        }
    }
}
