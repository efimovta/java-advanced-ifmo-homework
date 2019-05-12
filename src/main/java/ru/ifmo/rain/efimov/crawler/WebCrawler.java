package ru.ifmo.rain.efimov.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread-safe WebCrawler class that recurse bypass sites
 */
public class WebCrawler implements Crawler {

    private final TasksPerHostManager tasksPerHostManager;
    private final Downloader downloader;
    private final ExecutorService downloadersPool;
    private final ExecutorService extractorsPool;

    /**
     * @param downloader  allows to downloadAct pages and extract links from them; <br/>
     * @param downloaders the maximum number of simultaneously loaded pages; <br/>
     * @param extractors  the maximum number of pages from which links are extracted; <br/>
     * @param perHost     is the maximum number of pages simultaneously loaded from a single host. <br/>
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloadersPool = Executors.newFixedThreadPool(downloaders);
        this.extractorsPool = Executors.newFixedThreadPool(extractors);
        this.tasksPerHostManager = new TasksPerHostManager(perHost, downloadersPool);
    }

    /**
     * Recursively crawl pages starting at the specified URL to the specified depth
     * and return a list of unique downloaded pages and files.
     *
     * @param url   must have protocol, example: http://kgeorgiy.info/
     * @param depth depth
     */
    @Override
    public Result download(String url, int depth) {
        return new CrawlerDownloadAct(downloader, extractorsPool, tasksPerHostManager).downloadAct(url, depth);
    }

    /**
     * Shutdown all threads
     */
    @Override
    public void close() {
        downloadersPool.shutdownNow();
        extractorsPool.shutdownNow();
    }

    public static void main(String[] args) {
        if (null == args  || args.length == 0 || args.length > 5) {
            System.out.println("Command format: " +
                    "\n\tWebCrawler url [depth [downloads [extractors [perHost]]]]" +
                    "\nURL must have protocol, example: http://kgeorgiy.info/");
            return;
        }
        String target = args[0];
        int depth = 2;
        int downloads = Integer.MAX_VALUE;
        int extractors = Integer.MAX_VALUE;
        int perHost = Integer.MAX_VALUE;
        try {
            depth = Integer.parseInt(args[1]);
            downloads = Integer.parseInt(args[2]);
            extractors = Integer.parseInt(args[3]);
            perHost = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.out.println("Error, wrong number format: " + e.getMessage());
            return;
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        try (Crawler crawler = new WebCrawler(new CachingDownloader(), downloads, extractors, perHost)) {
            crawler.download(target, depth).getDownloaded().forEach(System.out::println);
        } catch (IOException e) {
            System.out.println("Cannot create temp dir: " + e.getMessage());
        }
    }
}
