package org.entur.auth.spring.common.health.indicator.jwks;

import static com.nimbusds.jose.jwk.source.JWKSetCacheRefreshEvaluator.noRefresh;
import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.health.contributor.Status.DOWN;
import static org.springframework.boot.health.contributor.Status.UP;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.val;
import org.entur.auth.spring.common.server.JWKSourceWithIssuer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Status;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwksHealthCache test suite")
class JwksHealthCacheTest {
    private static @NonNull JWKSet jwkSetFromKeys(final @NonNull JWK... keys) {
        return keys.length > 0 ? new JWKSet(asList(keys)) : new JWKSet();
    }

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

    private final @NonNull JwksHealthCache cache =
            new JwksHealthCache(clock, Runnable::run, ofSeconds(5));

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JWKSourceWithIssuer<?> source;

    @Nested
    @DisplayName("JwksHealthCache::get test suite")
    class GetTests {
        private static @NonNull Stream<Arguments> scenarios() {
            return Stream.of(
                    arguments(jwkSetFromKeys(), DOWN),
                    arguments(jwkSetFromKeys(new OctetSequenceKey.Builder(new byte[] {1}).build()), UP));
        }

        private static @NonNull Stream<Arguments> transitionScenarios() {
            return Stream.of(
                    arguments(
                            jwkSetFromKeys(),
                            jwkSetFromKeys(new OctetSequenceKey.Builder(new byte[] {1}).build()),
                            DOWN,
                            UP),
                    arguments(
                            jwkSetFromKeys(new OctetSequenceKey.Builder(new byte[] {1}).build()),
                            jwkSetFromKeys(),
                            UP,
                            DOWN));
        }

        @Test
        void should_fail_for_null_key() {
            assertThatThrownBy(() -> cache.get(null)).isInstanceOf(NullPointerException.class);
        }

        @ParameterizedTest
        @MethodSource("scenarios")
        void should_return_expected_status_on_first_access(
                final @NonNull JWKSet jwkSet, final @NonNull Status expected) throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(jwkSet);

            assertThat(cache.get(source)).isEqualTo(expected);

            verify(source.getJWKSetSource()).getJWKSet(eq(noRefresh()), anyLong(), isNull());
        }

        @Test
        void should_return_down_status_on_first_access_for_failing_jwks() throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenThrow(KeySourceException.class);

            assertThat(cache.get(source)).isEqualTo(DOWN);

            verify(source.getJWKSetSource()).getJWKSet(eq(noRefresh()), anyLong(), isNull());
        }

        @ParameterizedTest
        @MethodSource("scenarios")
        void should_not_refresh_expected_cached_status_when_within_ttl(
                final @NonNull JWKSet jwkSet, final @NonNull Status expected) throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(jwkSet);

            assertThat(cache.get(source)).isEqualTo(expected);

            instant.updateAndGet(t -> t.plusSeconds(4));

            assertThat(cache.get(source)).isEqualTo(expected);

            verify(source.getJWKSetSource()).getJWKSet(eq(noRefresh()), anyLong(), isNull());
        }

        @ParameterizedTest
        @MethodSource("scenarios")
        void should_return_expected_status_and_trigger_refresh_when_expired(
                final @NonNull JWKSet jwkSet, final @NonNull Status expected) throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(jwkSet);

            assertThat(cache.get(source)).isEqualTo(expected);

            instant.updateAndGet(t -> t.plusSeconds(6));

            assertThat(cache.get(source)).isEqualTo(expected);

            verify(source.getJWKSetSource(), times(2)).getJWKSet(eq(noRefresh()), anyLong(), isNull());
        }

        @ParameterizedTest
        @MethodSource("transitionScenarios")
        void should_return_expected_status_after_refresh(
                final @NonNull JWKSet jwkSetBeforeRefresh,
                final @NonNull JWKSet jwkSetAfterRefresh,
                final @NonNull Status statusBeforeRefresh,
                final @NonNull Status statusAfterRefresh)
                throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(jwkSetBeforeRefresh)
                    .thenReturn(jwkSetAfterRefresh);

            assertThat(cache.get(source)).isEqualTo(statusBeforeRefresh);

            instant.updateAndGet(t -> t.plusSeconds(6));

            assertThat(cache.get(source)).isEqualTo(statusAfterRefresh);

            verify(source.getJWKSetSource(), times(2)).getJWKSet(eq(noRefresh()), anyLong(), isNull());
        }

        @ParameterizedTest
        @MethodSource("scenarios")
        void should_not_trigger_multiple_refreshes_for_same_key(
                final @NonNull JWKSet jwkSet, final @NonNull Status expected) throws KeySourceException {
            when(source.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(jwkSet);

            assertThat(cache.get(source)).isEqualTo(expected);

            instant.updateAndGet(t -> t.plusSeconds(10));

            assertThat(cache.get(source)).isEqualTo(expected);
            assertThat(cache.get(source)).isEqualTo(expected);

            assertThat(cache.get(source)).isEqualTo(expected);

            verify(source.getJWKSetSource(), times(2)).getJWKSet(eq(noRefresh()), anyLong(), isNull());
        }

        @Test
        void should_handle_multiple_keys_independently() throws KeySourceException {
            val a = Mockito.<JWKSourceWithIssuer<?>>mock(RETURNS_DEEP_STUBS);
            val b = Mockito.<JWKSourceWithIssuer<?>>mock(RETURNS_DEEP_STUBS);

            when(a.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(jwkSetFromKeys(new OctetSequenceKey.Builder(new byte[] {1}).build()));

            when(b.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(jwkSetFromKeys());

            assertThat(cache.get(a)).isEqualTo(UP);
            assertThat(cache.get(b)).isEqualTo(DOWN);

            verify(a.getJWKSetSource()).getJWKSet(eq(noRefresh()), anyLong(), isNull());
            verify(b.getJWKSetSource()).getJWKSet(eq(noRefresh()), anyLong(), isNull());
        }

        @Test
        void should_refresh_each_key_independently_after_expiry() throws KeySourceException {
            val a = Mockito.<JWKSourceWithIssuer<?>>mock(RETURNS_DEEP_STUBS);
            val b = Mockito.<JWKSourceWithIssuer<?>>mock(RETURNS_DEEP_STUBS);

            when(a.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(jwkSetFromKeys(new OctetSequenceKey.Builder(new byte[] {1}).build()));

            when(b.getJWKSetSource().getJWKSet(eq(noRefresh()), anyLong(), isNull()))
                    .thenReturn(jwkSetFromKeys());

            assertThat(cache.get(a)).isEqualTo(UP);
            assertThat(cache.get(b)).isEqualTo(DOWN);

            instant.updateAndGet(t -> t.plusSeconds(10));

            assertThat(cache.get(a)).isEqualTo(UP);
            assertThat(cache.get(b)).isEqualTo(DOWN);

            verify(a.getJWKSetSource(), times(2)).getJWKSet(eq(noRefresh()), anyLong(), isNull());
            verify(b.getJWKSetSource(), times(2)).getJWKSet(eq(noRefresh()), anyLong(), isNull());
        }
    }
}
