package ru.ifmo.rain.vozleev;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public class IterativeParallelism implements ScalarIP {

    /**
     * Returns maximum value.
     *
     * @param threads    number or concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @return maximum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if not values are given.
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return task(threads, values, stream -> stream.max(comparator).orElseThrow(),
                stream -> stream.max(comparator).orElseThrow());
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number or concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @return minimum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if not values are given.
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator)
            throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    /**
     * Returns whether all values satisfies predicate.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether all values satisfies predicate or {@code true}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return task(threads, values, stream -> stream.allMatch(predicate), stream -> stream.allMatch(item -> item));
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads   number or concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether any value satisfies predicate or {@code false}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate)
            throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    private int threadsNumber;
    private int eachCount;
    private int restCount;
    private List<Thread> threads;

    private <T, R> R task(int threads, List<? extends T> values, Function<Stream<? extends T>, R> mapper,
                          Function<Stream<R>, R> resultGrabber)
            throws InterruptedException {
        init(threads, values);
        var threadValues = new ArrayList<R>(Collections.nCopies(threadsNumber, null));
        addThreads(threadValues, values, mapper);
        joinThreads();
        return resultGrabber.apply(threadValues.stream());
    }

    private <T> void init(int threads, List<? extends T> values) {
        validateInput(threads, values);
        threadsNumber = Math.max(1, Math.min(threads, values.size()));
        this.threads = new ArrayList<>();
        var count = values.size();
        eachCount = count / threadsNumber;
        restCount = count % threadsNumber;
    }

    private <T> void validateInput(int threadsNumbers, List<? extends T> values) {
        if (threadsNumbers <= 0) {
            throw new IllegalArgumentException("ERROR! Less than one thread");
        }
        Objects.requireNonNull(values);
    }

    private <T, R> void addThreads(List<R> threadValues, List<? extends T> values,
                                   Function<Stream<? extends T>, R> mapper) {
        for (int j = 0, l, r = 0; j < threadsNumber; j++) {
            l = r;
            r = l + eachCount;
            if (restCount > 0) {
                r++;
            }
            restCount--;
            addThread(threads, threadValues, j, values.subList(l, r).stream(), mapper);
        }
    }

    private <T, R> void addThread(List<Thread> threads, List<R> threadValues, int index,
                                  Stream<? extends T> stream, Function<Stream<? extends T>, R> mapper) {
        var thread = new Thread(() -> threadValues.set(index, mapper.apply(stream)));
        thread.start();
        threads.add(thread);
    }

    private void joinThreads()
            throws InterruptedException {
        for (var thread : this.threads) {
            thread.join();
        }
    }
}