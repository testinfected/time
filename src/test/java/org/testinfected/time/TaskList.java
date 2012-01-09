package org.testinfected.time;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TaskList<T> {

    private final List<Future<T>> tasks = new ArrayList<Future<T>>();

    public void add(Future<T> task) {
        tasks.add(task);
    }

    public List<T> getResults(long timeoutInMillis) throws ExecutionException, InterruptedException, TimeoutException {
        final List<T> results = new ArrayList<T>();
        for (Future<T> task : tasks) {
            results.add(task.get(timeoutInMillis, TimeUnit.MILLISECONDS));
        }
        return results;
    }

    public T getSingleResult(long timeoutInMillis) throws ExecutionException, InterruptedException, TimeoutException {
        return getResults(timeoutInMillis).iterator().next();
    }

    public int await(long timeoutInMillis) throws ExecutionException, InterruptedException, TimeoutException {
        final List<T> results = getResults(timeoutInMillis);
        return results.size();
    }
}