package org.entur.auth.spring.common.cache;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.function.Predicate.not;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class SwrCache<K, V> {
    private final @NonNull Clock clock;
    private final @NonNull Executor executor;
    private final @NonNull Duration ttl;
    private final @NonNull Function<@NonNull K, @NonNull V> loader;

    private final @NonNull ConcurrentHashMap<
                    @NonNull K, @NonNull CompletableFuture<@NonNull Entry<@NonNull V>>>
            cache = new ConcurrentHashMap<>();

    public @NonNull V get(final @NonNull K key) {
        val entry = cache.computeIfAbsent(key, this::supplyEntry).join();

        if (entry.isFresh(clock.instant(), ttl)) return entry.value;

        refresh(key);

        return entry.value;
    }

    private @NonNull CompletableFuture<@NonNull Entry<@NonNull V>> supplyEntry(final @NonNull K key) {
        return supplyAsync(() -> new Entry<>(loader.apply(key), clock.instant()), executor)
                .whenComplete(
                        (entry, exception) -> ofNullable(exception).ifPresent(ignored -> cache.remove(key)));
    }

    private void refresh(final @NonNull K key) {
        cache.compute(
                key,
                (k, previous) ->
                        ofNullable(previous)
                                .filter(not(CompletableFuture::isDone))
                                .orElseGet(() -> supplyEntry(key)));
    }

    private record Entry<V>(@NonNull V value, @NonNull Instant createdAt) {
        public boolean isFresh(final @NonNull Instant now, final @NonNull Duration ttl) {
            return createdAt.plus(ttl).isAfter(now);
        }
    }
}
