package ru.ifmo.rain.efimov.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class ParallelMapperImpl implements ParallelMapper {

    private final TaskManager taskManager;

    public ParallelMapperImpl(int threads) {
        taskManager = new TaskManager(threads);
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<Task<? super T, ? extends R>> tasks = new ArrayList<>();
        for (T arg : args) {
            Task<? super T, ? extends R> task = new Task<>(f, arg);
            tasks.add(task);
            taskManager.addTask(task);
        }
        List<R> result = new ArrayList<>();
        for (Task<? super T, ? extends R> task : tasks) {
            result.add(task.getResult());
        }
        return result;
    }

    @Override
    public void close() {
        taskManager.shutdown();
    }
}
