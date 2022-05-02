package com.streamcollector.app.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class FutureUtils {
    public static <T> CompletableFuture<List<T>> allOf(
            Collection<CompletableFuture<T>> futures) {
        return futures.stream()
                .collect(collectingAndThen(
                        toList(),
                        l -> CompletableFuture.allOf(l.toArray(new CompletableFuture[0]))
                                .thenApply(__ -> l.stream()
                                        .map(CompletableFuture::join)
                                        .collect(Collectors.toList()))));
    }

    public static <T> CompletableFuture<T> anyOf(List<CompletableFuture<T>> cfs) {
        return CompletableFuture.anyOf(cfs.toArray(new CompletableFuture[0]))
                .thenApply(o -> (T) o);
    }

    public static <T> CompletableFuture<T> anyOf(CompletableFuture<T>... cfs) {
        return CompletableFuture.anyOf(cfs).thenApply(o -> (T) o);
    }
}
