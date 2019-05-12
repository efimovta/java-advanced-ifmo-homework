package ru.ifmo.rain.efimov.crawler;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

/**
 * Ensure that simultaneous tasks to the same host are less than the specified number.
 */
class TasksPerHostManager {
    private final int perhost;
    private final Map<String, Integer> onHost;
    private final Map<String, Queue<Runnable>> pending;
    private final ExecutorService downloadersPool;

    TasksPerHostManager(int perHost, ExecutorService downloadersPool) {
        this.perhost = perHost;
        this.downloadersPool = downloadersPool;

        this.onHost = new HashMap<>();
        this.pending = new HashMap<>();
    }

    synchronized void putTask(String host, Runnable task) {
        onHost.putIfAbsent(host, 0);
        if (onHost.get(host) < perhost) {
            onHost.put(host, onHost.get(host) + 1);
            downloadersPool.submit(task);
        } else {
            pending.putIfAbsent(host, new LinkedList<>());
            pending.get(host).add(task);
        }

    }

    synchronized void contDown(String host) {
        Queue<Runnable> queue = pending.get(host);
        if (null != queue) {
            Runnable runnable = queue.poll();
            if (null != runnable) {
                downloadersPool.submit(runnable);
            } else {
                pending.remove(host);
            }
        } else {
            onHost.put(host, onHost.get(host) - 1);
        }
    }
}