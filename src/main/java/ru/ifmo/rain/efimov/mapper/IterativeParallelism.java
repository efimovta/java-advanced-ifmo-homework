package ru.ifmo.rain.efimov.mapper;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class IterativeParallelism implements ListIP {

    private final ParallelMapper parallelMapper;

    public IterativeParallelism() {
        this.parallelMapper = null;
    }

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        Function<List<?>, String> joinFunc = list -> list.stream().map(Object::toString).collect(Collectors.joining());
        List<String> threadsResults = concurrent(threads, values, joinFunc);
        return joinFunc.apply(threadsResults);
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        Function<List<? extends T>, List<T>> filterFunc = arg -> arg.stream().filter(predicate).collect(Collectors.toList());
        List<List<T>> threadsResults = concurrent(threads, values, filterFunc);
        return flatList(threadsResults);
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> function) throws InterruptedException {
        Function<List<? extends T>, List<U>> mapFunc = arg -> arg.stream().map(function).collect(Collectors.toList());
        List<List<U>> threadsResults = concurrent(threads, values, mapFunc);
        return flatList(threadsResults);
    }

    @Override
    public <T> T maximum(int i, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException, NoSuchElementException {
        return minimum(i, values, comparator.reversed());
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException, NoSuchElementException {
        Function<List<? extends T>, T> minFunc = arg -> arg.stream().min(comparator).get();
        List<T> threadsResults = concurrent(threads, values, minFunc);
        return minFunc.apply(threadsResults);
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !any(threads, values, predicate.negate());
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        List<Boolean> threadsResults = concurrent(threads, values, list -> list.stream().anyMatch(predicate));
        return threadsResults.stream().anyMatch(Predicate.isEqual(true));
    }

    /**
     * Splits values by threads to equal parts (as possible, or +1 in some parts),
     * give parts to ParallelMapper#map(Function, List) or creates threads itself,
     * apply function to parts, return list of threads results.
     *
     * @return list of threads results
     * @see ParallelMapper#map(Function, List)
     */
    private <T, R> List<R> concurrent(int threads, List<? extends T> values, Function<List<? extends T>, R> func) throws InterruptedException {
        threads = Math.min(threads, values.size());
        List<List<? extends T>> parts = split(values, threads);

        if (parallelMapper != null) {
            return parallelMapper.map(func, parts);
        }

        List<FunctionRunnable<T, R>> runnableList = parts.stream().map(args -> new FunctionRunnable<>(func, args)).collect(Collectors.toList());
        List<Thread> threadList = runnableList.stream().map(Thread::new).collect(Collectors.toList());
        threadList.forEach(Thread::start);
        for (Thread t : threadList) {
            t.join();
        }
        return runnableList.stream().map(FunctionRunnable::getResult).collect(Collectors.toList());
    }

    private <T> List<List<? extends T>> split(List<? extends T> list, int n) {
        List<List<? extends T>> parts = new ArrayList<>();
        int step = list.size() / n;
        int to = 0;
        for (int i = 0; i < n; i += 1) {
            int from = to;
            to += step + (i < (list.size() % n) ? 1 : 0);
            parts.add(list.subList(from, to));
        }
        return parts;
    }


    private <T> List<T> flatList(List<List<T>> listOfList) {
        return listOfList.stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private class FunctionRunnable<X, Y> implements Runnable {
        private List<? extends X> values;
        private Function<List<? extends X>, Y> func;
        private Y result;

        public FunctionRunnable(Function<List<? extends X>, Y> func, List<? extends X> values) {
            this.func = func;
            this.values = values;
        }

        @Override
        public void run() {
            result = func.apply(values);
        }

        public Y getResult() {
            return result;
        }
    }
}
