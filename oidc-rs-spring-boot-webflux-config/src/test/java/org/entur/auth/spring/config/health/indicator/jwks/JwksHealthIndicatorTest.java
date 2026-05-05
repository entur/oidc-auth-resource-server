package org.entur.auth.spring.config.health.indicator.jwks;

import static java.util.Collections.emptyList;
import static java.util.Collections.nCopies;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;

import org.entur.auth.spring.common.server.JWKSourceWithIssuer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwksHealthIndicator test suite")
class JwksHealthIndicatorTest {
    @Mock private JWKSourceWithIssuer<?> source;

    @Mock private JwksHealthCache cache;

    @Nested
    @DisplayName("JwksHealthIndicator::health test suite")
    class HealthTests {
        @Test
        void should_return_unknown_for_empty_sources() {
            assertThat(new JwksHealthIndicator(emptyList(), cache).health())
                    .isEqualTo(Health.unknown().build());

            verifyNoInteractions(cache);
        }

        @ParameterizedTest
        @ValueSource(
                shorts = {
                    1, 2, 3,
                })
        void should_return_up_if_all_sources_are_healthy(final short numberOfSources) {
            when(cache.get(any(JWKSourceWithIssuer.class))).thenReturn(UP);

            assertThat(new JwksHealthIndicator(nCopies(numberOfSources, source), cache).health())
                    .isEqualTo(Health.up().build());

            verify(cache, times(numberOfSources)).get(any(JWKSourceWithIssuer.class));
        }

        @ParameterizedTest
        @ValueSource(
                shorts = {
                    1, 2, 3,
                })
        void should_return_down_if_any_source_fails(final short numberOfSources) {
            when(cache.get(any(JWKSourceWithIssuer.class))).thenReturn(DOWN);

            assertThat(new JwksHealthIndicator(nCopies(numberOfSources, source), cache).health())
                    .isEqualTo(Health.down().build());

            verify(cache, atMostOnce()).get(any(JWKSourceWithIssuer.class));
        }
    }
}
