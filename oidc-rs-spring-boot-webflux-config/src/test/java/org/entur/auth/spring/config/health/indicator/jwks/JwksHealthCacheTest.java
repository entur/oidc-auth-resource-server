package org.entur.auth.spring.config.health.indicator.jwks;

import static com.nimbusds.jose.jwk.source.JWKSetCacheRefreshEvaluator.noRefresh;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.boot.actuate.health.Status.UP;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.NonNull;
import lombok.val;
import org.entur.auth.spring.common.server.JWKSourceWithIssuer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwksHealthCache test suite")
class JwksHealthCacheTest {
    private final @NonNull AtomicReference<Instant> instant = new AtomicReference<>(Instant.now());

    private final @NonNull Clock clock =
            new Clock() {
                @Override
                public ZoneId getZone() {
                    return null;
                }

                @Override
                public Clock withZone(ZoneId zone) {
                    return null;
                }

                @Override
                public Instant instant() {
                    return instant.get();
                }
            };

    private final @NonNull AtomicInteger executions = new AtomicInteger();

    private final @NonNull Executor executor =
            command -> {
                executions.incrementAndGet();

                command.run();
            };

    private final @NonNull JwksHealthCache cache = new JwksHealthCache(clock, executor, ofSeconds(5));

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JWKSourceWithIssuer<?> source;

    @Nested
    @DisplayName("JwksHealthCache::get test suite")
    class GetTests {
        @Test
        void should_fail_for_null_key() {
            assertThatThrownBy(() -> cache.get(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void should_return_up_status_on_first_access_for_non_empty_jwks() throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet(new OctetSequenceKey.Builder(new byte[] {1}).build()));

            val status = cache.get(source);

            assertThat(status).isEqualTo(UP);
            assertThat(executions.get()).isEqualTo(1);
        }

        @Test
        void should_return_down_status_on_first_access_for_empty_jwks() throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet());

            val status = cache.get(source);

            assertThat(status).isEqualTo(DOWN);
            assertThat(executions.get()).isEqualTo(1);
        }

        @Test
        void should_return_down_status_on_first_access_for_failing_jwks() throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenThrow(KeySourceException.class);

            val status = cache.get(source);

            assertThat(status).isEqualTo(DOWN);
            assertThat(executions.get()).isEqualTo(1);
        }

        @Test
        void should_return_cached_up_status_during_fresh() throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet(new OctetSequenceKey.Builder(new byte[] {1}).build()));

            cache.get(source);

            val status = cache.get(source);

            assertThat(status).isEqualTo(UP);
            assertThat(executions.get()).isEqualTo(1);
        }

        @Test
        void should_return_cached_down_status_during_fresh() throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet())
                    .thenThrow(KeySourceException.class);

            cache.get(source);

            val status = cache.get(source);

            assertThat(status).isEqualTo(DOWN);
            assertThat(executions.get()).isEqualTo(1);
        }

        @Test
        void should_not_refresh_cached_up_status_when_within_ttl() throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet(new OctetSequenceKey.Builder(new byte[] {1}).build()));

            cache.get(source);

            instant.updateAndGet(t -> t.plusSeconds(4));

            val status = cache.get(source);

            assertThat(status).isEqualTo(UP);
            assertThat(executions.get()).isEqualTo(1);
        }

        @Test
        void should_not_refresh_cached_down_status_when_within_ttl() throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet())
                    .thenThrow(KeySourceException.class);

            cache.get(source);

            instant.updateAndGet(t -> t.plusSeconds(4));

            val status = cache.get(source);

            assertThat(status).isEqualTo(DOWN);
            assertThat(executions.get()).isEqualTo(1);
        }

        @Test
        void should_return_stale_up_status_and_trigger_refresh_when_expired()
                throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet(new OctetSequenceKey.Builder(new byte[] {1}).build()));

            cache.get(source);

            instant.updateAndGet(t -> t.plusSeconds(10));

            val status = cache.get(source);

            assertThat(status).isEqualTo(UP);
            assertThat(executions.get()).isEqualTo(2);
        }

        @Test
        void should_return_stale_down_status_and_trigger_refresh_when_expired()
                throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet())
                    .thenThrow(KeySourceException.class);

            cache.get(source);

            instant.updateAndGet(t -> t.plusSeconds(10));

            val status = cache.get(source);

            assertThat(status).isEqualTo(DOWN);
            assertThat(executions.get()).isEqualTo(2);
        }

        @Test
        void should_return_updated_up_status_after_refresh() throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet())
                    .thenReturn(new JWKSet(new OctetSequenceKey.Builder(new byte[] {1}).build()));

            cache.get(source);

            instant.updateAndGet(t -> t.plusSeconds(10));

            cache.get(source);

            val status = cache.get(source);

            assertThat(status).isEqualTo(UP);
            assertThat(executions.get()).isEqualTo(2);
        }

        @Test
        void should_return_updated_down_status_after_refresh() throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet(new OctetSequenceKey.Builder(new byte[] {1}).build()))
                    .thenReturn(new JWKSet());

            cache.get(source);

            instant.updateAndGet(t -> t.plusSeconds(10));

            cache.get(source);

            val status = cache.get(source);

            assertThat(status).isEqualTo(DOWN);
            assertThat(executions.get()).isEqualTo(2);
        }

        @Test
        void should_not_trigger_multiple_refreshes_for_same_key() {
            cache.get(source);

            instant.updateAndGet(t -> t.plusSeconds(10));

            cache.get(source);
            cache.get(source);
            cache.get(source);

            assertThat(executions.get()).isEqualTo(2);
        }

        @Test
        void should_handle_multiple_keys_independently() throws KeySourceException {
            val a = Mockito.<JWKSourceWithIssuer<?>>mock(RETURNS_DEEP_STUBS);
            val b = Mockito.<JWKSourceWithIssuer<?>>mock(RETURNS_DEEP_STUBS);

            when(a.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet(new OctetSequenceKey.Builder(new byte[] {1}).build()));

            when(b.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet());

            cache.get(a);
            cache.get(b);

            assertThat(executions.get()).isEqualTo(2);
        }

        @Test
        void should_refresh_each_key_independently_after_expiry() throws KeySourceException {
            val a = Mockito.<JWKSourceWithIssuer<?>>mock(RETURNS_DEEP_STUBS);
            val b = Mockito.<JWKSourceWithIssuer<?>>mock(RETURNS_DEEP_STUBS);

            when(a.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet(new OctetSequenceKey.Builder(new byte[] {1}).build()));

            when(b.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(new JWKSet());

            cache.get(a);
            cache.get(b);

            instant.updateAndGet(t -> t.plusSeconds(10));

            cache.get(a);
            cache.get(b);

            assertThat(executions.get()).isEqualTo(4);
        }
    }
}
