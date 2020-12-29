package com.nickrobison.tuple;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by cliff on 5/4/14.
 */
public class TuplePool<T> {
    private final ThreadLocal<ArrayDeque<T>> pool;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<Initializer<T>> initializer;
    private final Loader<T> loader;
    private final Destroyer<T> destroyer;
    private final CopyOnWriteArrayList<T[]> references;
    private int size;
    private final int reloadSize;
    private final boolean createWhenExhausted;
    private volatile boolean closed = false;

    public TuplePool(int size,
                     boolean createWhenExhausted,
                     Loader<T> loader,
                     Destroyer<T> destroyer) {
        this(size, createWhenExhausted, loader, destroyer, null);
    }

    public TuplePool(final int size,
                     boolean createWhenExhausted,
                     Loader<T> loader,
                     Destroyer<T> destroyer,
                     Initializer<T> initializer) {
        this.size = 0;
        this.reloadSize = size;
        this.createWhenExhausted = createWhenExhausted;
        this.references = new CopyOnWriteArrayList<>();
        this.loader = loader;
        this.destroyer = destroyer;
        this.initializer = Optional.ofNullable(initializer);
        this.pool = ThreadLocal.withInitial(() -> {
            ArrayDeque<T> deque = new ArrayDeque<>(size);
            reload(deque);
            return deque;
        });
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
        initializer.ifPresent(tInitializer -> tInitializer.initialize(obj));
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
        if (closed) {
            throw new IllegalStateException("Pool's closed everyone out!");
        }
        try {
            final T[] tuples = loader.createArray(reloadSize);
            size += reloadSize;
            for (T tuple : tuples) {
                deque.push(tuple);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to reload Tuple pool");
        }
    }

    public void close() {
        closed = true;
        for (T[] ary : references) {
            destroyer.destroyArray(ary);
        }
        references.clear();
        pool.remove();
    }
}
