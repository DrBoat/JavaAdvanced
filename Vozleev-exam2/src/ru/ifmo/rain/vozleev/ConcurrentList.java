package ru.ifmo.rain.vozleev;

import java.util.*;

class ConcurrentList<T> extends AbstractList<T> {
    private final List<T> list;
    private int finished = 0;

    ConcurrentList() {
        this(Collections.emptyList());
    }

    ConcurrentList(Collection<T> c) {
        list = new ArrayList<>(c);
    }

    List<T> getList() throws InterruptedException {
        synchronized (this) {
            while (finished != list.size()) {
                wait();
            }
        }
        return list;
    }

    void increment() {
        synchronized (this) {
            finished++;
            notify();
        }
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public T set(int index, T element) {
        list.set(index, element);
        return list.get(index);
    }

    @Override
    public int size() {
        return list.size();
    }
}
