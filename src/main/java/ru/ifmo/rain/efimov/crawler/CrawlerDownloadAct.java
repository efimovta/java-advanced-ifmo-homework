package ru.ifmo.rain.efimov.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;

/**
 * It is a single crawler act of following a link to a specified depth. <br/>
 * Downloading and or extracting links from the same page within one
 * crawler act is prohibited.
 */
class CrawlerDownloadAct {

    private final TasksPerHostManager tasksPerHostManager;
    private final Downloader downloader;
    private final ExecutorService extractorsThreadPool;

    private final Phaser phaser;
    private final Set<String> processed;
    private final Map<String, IOException> errors;

    CrawlerDownloadAct(Downloader downloader, ExecutorService extractorsThreadPool, TasksPerHostManager tasksPerHostManager) {
        this.tasksPerHostManager = tasksPerHostManager;
        this.extractorsThreadPool = extractorsThreadPool;
        this.downloader = downloader;

        processed = ConcurrentHashMap.newKeySet();
        errors = new ConcurrentHashMap<>();
        phaser = new Phaser(1);
    }

    Result downloadAct(String url, int depth) {
        download(url, depth);
        phaser.arriveAndAwaitAdvance();
        processed.removeAll(errors.keySet());
        return new Result(new ArrayList<>(processed), errors);
    }

    private void download(String url, int depth) {
        if (depth == 0 || !processed.add(url)) {
            return;
        }
        String host;
        try {
            host = URLUtils.getHost(url);
        } catch (MalformedURLException e) {
            errors.put(url, e);
            return;
        }

        phaser.register();
        tasksPerHostManager.putTask(host, () -> {
            try {
                Document doc = downloader.download(url);
                extract(doc, depth, url);
            } catch (IOException e) {
                errors.put(url, e);
            } finally {
                phaser.arrive();
                tasksPerHostManager.contDown(host);
            }
        });
    }

    private void extract(Document doc, int depth, String url) {
        phaser.register();
        extractorsThreadPool.submit(() -> {
            try {
                doc.extractLinks().stream().filter(to -> !processed.contains(to)).forEach(to -> {
                    download(to, depth - 1);
                });
            } catch (IOException e) {
                errors.put(url, e);
            } finally {
                phaser.arrive();
            }
        });
    }
}
