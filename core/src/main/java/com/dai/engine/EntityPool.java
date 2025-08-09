package com.dai.engine;

import java.util.LinkedList;
import java.util.Queue;

public abstract class EntityPool<T> {

    private Queue<T> pool;
    private int size = 1;

    // protected EntityPool(int size) {}

    /** Do not forget to call this! */
    protected void init(int size) {
        this.size = size;
        this.pool = new LinkedList<T>();

        for(int i=0; i<size; i++) { pool.add(this.create()); }
    }

    public T borrowFrom() {
        if(pool.isEmpty()) {
            // Expand pool
            for(int i=0; i<size; i++) { pool.add(this.create()); }

            return pool.poll();
        }

        return pool.poll();
    }

    public void returnTo(T t) {
        if(t != null) {
            pool.offer(t);
        }
    }

    protected abstract T create();
}
