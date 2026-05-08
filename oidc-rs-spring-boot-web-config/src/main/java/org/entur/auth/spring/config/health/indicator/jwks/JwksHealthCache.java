package org.entur.auth.spring.config.health.indicator.jwks;

import static com.nimbusds.jose.jwk.source.JWKSetCacheRefreshEvaluator.noRefresh;
import static java.lang.System.currentTimeMillis;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;

import com.nimbusds.jose.KeySourceException;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executor;
import lombok.NonNull;
import lombok.val;
import org.entur.auth.spring.common.cache.SwrCache;
import org.entur.auth.spring.config.server.JWKSourceWithIssuer;
import org.springframework.boot.actuate.health.Status;

public final class JwksHealthCache extends SwrCache<JWKSourceWithIssuer, Status> {
    public JwksHealthCache(
            final @NonNull Clock clock, final @NonNull Executor executor, final @NonNull Duration ttl) {
        super(clock, executor, ttl, JwksHealthCache::loader);
    }

    private static @NonNull Status loader(final @NonNull JWKSourceWithIssuer source) {
        try {
            val jwkSet = source.getJWKSetSource().getJWKSet(noRefresh(), currentTimeMillis(), null);

            return jwkSet.isEmpty() ? DOWN : UP;
        } catch (KeySourceException e) {
            return DOWN;
        }
    }
}
