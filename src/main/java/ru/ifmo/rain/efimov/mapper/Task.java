package ru.ifmo.rain.efimov.mapper;


import java.util.function.Function;

public class Task<X, Y> {
    private final X arg;
    private final Function<? super X, ? extends Y> func;
    private Y result;
    private boolean completed = false;

    Task(Function<? super X, ? extends Y> func, X arg) {
        this.arg = arg;
        this.func = func;
    }

    synchronized void run() {
        result = func.apply(arg);
        completed = true;
        notify();
    }

    synchronized Y getResult() throws InterruptedException {
        while (!completed) {
            wait();
        }
        return result;
    }
}
