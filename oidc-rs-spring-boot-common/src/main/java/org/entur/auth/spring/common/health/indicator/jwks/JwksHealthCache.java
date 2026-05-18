package org.entur.auth.spring.common.health.indicator.jwks;

import static com.nimbusds.jose.jwk.source.JWKSetCacheRefreshEvaluator.noRefresh;
import static java.lang.System.currentTimeMillis;
import static java.time.Duration.between;
import static java.time.Instant.EPOCH;
import static java.util.Optional.ofNullable;
import static org.springframework.boot.health.contributor.Status.DOWN;
import static org.springframework.boot.health.contributor.Status.UNKNOWN;
import static org.springframework.boot.health.contributor.Status.UP;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nimbusds.jose.KeySourceException;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executor;
import lombok.NonNull;
import lombok.val;
import org.entur.auth.spring.common.server.JWKSourceWithIssuer;
import org.springframework.boot.health.contributor.Status;

public final class JwksHealthCache {
    private final @NonNull LoadingCache<@NonNull JWKSourceWithIssuer<?>, Status> cache;

    public JwksHealthCache(
            final @NonNull Clock clock, final @NonNull Executor executor, final @NonNull Duration ttl) {
        this.cache =
                Caffeine.newBuilder()
                        .refreshAfterWrite(ttl)
                        .executor(executor)
                        .ticker(() -> between(EPOCH, clock.instant()).toNanos())
                        .build(JwksHealthCache::loader);
    }

    public @NonNull Status get(final @NonNull JWKSourceWithIssuer<?> source) {
        return ofNullable(cache.get(source)).orElse(UNKNOWN);
    }

    private static @NonNull Status loader(final @NonNull JWKSourceWithIssuer<?> source) {
        try {
            val jwkSet = source.getJWKSetSource().getJWKSet(noRefresh(), currentTimeMillis(), null);

            return jwkSet.isEmpty() ? DOWN : UP;
        } catch (KeySourceException e) {
            return DOWN;
        }
    }
}
