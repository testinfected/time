package org.testinfected.time;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelExecutor {
    public static final int DEFAULT_NUMBER_OF_THREADS_TO_RUN_CONCURRENTLY = 2;

    private final ExecutorService executorService;

    public ParallelExecutor() {
        this(DEFAULT_NUMBER_OF_THREADS_TO_RUN_CONCURRENTLY);
    }

    public ParallelExecutor(int threadCount) {
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    public <T> Execution<T> spawn(final Callable<T> task) {
        return spawn(task, 1);
    }

    public <T> Execution<T> spawn(final Callable<T> task, int taskCount) {
        final Execution<T> pending = new Execution<T>();

        for (int i = 0; i < taskCount; i++) {
            pending.add(executorService.submit(task));
        }

        return pending;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}