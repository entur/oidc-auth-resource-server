package org.entur.auth.spring.config.health.indicator.jwks;

import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;

import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.entur.auth.spring.common.server.JWKSourceWithIssuer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

@RequiredArgsConstructor
public final class JwksHealthIndicator implements HealthIndicator {
    private final @NonNull List<@NonNull JWKSourceWithIssuer<?>> sources;
    private final @NonNull JwksHealthCache cache;

    @Override
    public Health health() {
        if (sources.isEmpty()) return Health.unknown().build();

        return Health.status(sources.stream().map(cache::get).allMatch(UP::equals) ? UP : DOWN).build();
    }
}
