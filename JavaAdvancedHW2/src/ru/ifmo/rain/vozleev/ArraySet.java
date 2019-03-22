package ru.ifmo.rain.vozleev;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements SortedSet<T> {

    private final List<T> data;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        comparator = null;
        data = Collections.emptyList();
    }

    public ArraySet(Collection<? extends T> other) {
        this(other, null);
    }

    public ArraySet(Comparator<? super T> comparator) {
        this.comparator = comparator;
        data = Collections.emptyList();
    }

    public ArraySet(Collection<? extends T> other, Comparator<? super T> comparator) {
        this.comparator  = comparator;
        Set<T> tmpSet = new TreeSet<>(this.comparator);
        tmpSet.addAll(other);
        data = new ArrayList<>(tmpSet);
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return headSet(toElement).tailSet(fromElement);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return new ArraySet<>(data.subList(0, bin(toElement, true, false) + 1), comparator);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return new ArraySet<>(data.subList(bin(fromElement, false, true), data.size()), comparator);
    }

    @Override
    public T first() {
        checkNonEmpty();
        return data.get(0);
    }

    @Override
    public T last() {
        checkNonEmpty();
        return data.get(data.size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        try {
            return Collections.binarySearch(this.data, (T) o, this.comparator()) >= 0;
        } catch (ClassCastException e) {
            System.err.println("ERROR! (In method contains) argument has unexpected type");
            return false;
        }
    }

    private void checkNonEmpty() {
        if (data.isEmpty()) {
            throw new NoSuchElementException();
        }
    }

    private int bin(T element, boolean to, boolean inclusive) {

        int pos = Collections.binarySearch(data, element, comparator);
        if (pos < 0) {
            pos = ~pos - (to ? 1 : 0);
        } else if (!inclusive) {
            pos += (to ? -1 : 1);
        }
        return pos;
    }
}
