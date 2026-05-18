package org.entur.auth.spring.common.health.indicator.jwks;

import static org.springframework.boot.health.contributor.Status.DOWN;
import static org.springframework.boot.health.contributor.Status.UP;

import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.entur.auth.spring.common.server.JWKSourceWithIssuer;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

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
