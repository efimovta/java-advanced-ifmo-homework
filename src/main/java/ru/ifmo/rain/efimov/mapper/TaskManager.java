package ru.ifmo.rain.efimov.mapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TaskManager {
    private final List<Thread> threadList = new ArrayList<>();
    private final SynchronizedTaskQueue taskQueue = new SynchronizedTaskQueue();


    public TaskManager(int threads) {
        Worker worker = new Worker(taskQueue);
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(worker);
            threadList.add(t);
            t.start();
        }
    }

    public <R, T> void addTask(Task<? super T, ? extends R> task) {
        taskQueue.addTask(task);
    }

    public void shutdown() {
        threadList.forEach(Thread::interrupt);
    }

    private class Worker implements Runnable {
        private final SynchronizedTaskQueue tasks;

        Worker(SynchronizedTaskQueue tasks) {
            this.tasks = tasks;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    tasks.getTask().run();
                }
            } catch (InterruptedException e) {
                //nothing
            } finally {
                Thread.currentThread().interrupt();
            }
        }
    }

    private class SynchronizedTaskQueue {
        private final Queue<Task<?, ?>> tasks = new LinkedList<>();

        synchronized void addTask(Task<?, ?> input) {
            tasks.add(input);
            notify();
        }

        synchronized Task<?, ?> getTask() throws InterruptedException {
            while (tasks.isEmpty()) {
                wait(); //passive waiting
            }
            return tasks.poll();
        }
    }
}
