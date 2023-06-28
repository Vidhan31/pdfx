package com.pdfx.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Singleton class to prevent multiple executor service instances
 */
public class SingletonExecutorService {

    private static ExecutorService executor;

    private SingletonExecutorService() {}

    /**
     * @return new {@code ExecutorService} instance first time, otherwise returns existing one.
     */
    public static ExecutorService getInstance() {

        if (executor == null) {

            executor = new ThreadPoolExecutor(
                    3,
                    Runtime.getRuntime().availableProcessors() - 1,
                    90L,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(500),
                    new ThreadPoolExecutor.AbortPolicy()
            );
        }

        return executor;
    }

    public static void close() {

        if (executor != null) {

            try {
                getInstance().shutdown();
            } catch (Exception e) {
                e.printStackTrace();
                getInstance().shutdownNow();
            }
        }
    }
}

