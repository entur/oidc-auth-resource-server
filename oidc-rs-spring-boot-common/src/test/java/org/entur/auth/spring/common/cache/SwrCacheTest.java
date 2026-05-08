package org.entur.auth.spring.common.cache;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.NonNull;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SwrCache test suite")
class SwrCacheTest {
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

    private final @NonNull AtomicInteger cachedValue = new AtomicInteger();

    private final @NonNull AtomicInteger executions = new AtomicInteger();

    private final @NonNull Executor executor =
            command -> {
                executions.incrementAndGet();

                command.run();
            };

    private final @NonNull SwrCache<String, Integer> cache =
            new SwrCache<>(clock, executor, ofSeconds(5), key -> cachedValue.incrementAndGet());

    @Nested
    @DisplayName("SwrCache::get test suite")
    class GetTests {
        @Test
        void should_fail_for_null_key() {
            assertThatThrownBy(() -> cache.get(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void should_fail_for_loader_that_returns_null() {
            assertThatThrownBy(() -> new SwrCache<>(clock, executor, ofSeconds(5), key -> null).get(""))
                    .isInstanceOf(CompletionException.class)
                    .hasCauseInstanceOf(NullPointerException.class);
        }

        @Test
        void should_return_value_on_first_access() {
            val value = cache.get("a");

            assertThat(value).isEqualTo(1);
            assertThat(executions.get()).isEqualTo(1);
        }

        @Test
        void should_return_cached_value_during_fresh() {
            cache.get("a");

            val value = cache.get("a");

            assertThat(value).isEqualTo(1);
            assertThat(executions.get()).isEqualTo(1);
        }

        @Test
        void should_not_refresh_when_within_ttl() {
            cache.get("a");

            instant.updateAndGet(t -> t.plusSeconds(4));

            val value = cache.get("a");

            assertThat(value).isEqualTo(1);
            assertThat(executions.get()).isEqualTo(1);
        }

        @Test
        void should_return_stale_value_and_trigger_refresh_when_expired() {
            cache.get("a");

            instant.updateAndGet(t -> t.plusSeconds(10));

            val value = cache.get("a");

            assertThat(value).isEqualTo(1);
            assertThat(executions.get()).isEqualTo(2);
        }

        @Test
        void should_return_updated_value_after_refresh() {
            cache.get("a");

            instant.updateAndGet(t -> t.plusSeconds(10));

            cache.get("a");

            val value = cache.get("a");

            assertThat(value).isEqualTo(2);
            assertThat(executions.get()).isEqualTo(2);
        }

        @Test
        void should_not_trigger_multiple_refreshes_for_same_key() {
            cache.get("a");

            instant.updateAndGet(t -> t.plusSeconds(10));

            cache.get("a");
            cache.get("a");
            cache.get("a");

            assertThat(executions.get()).isEqualTo(2);
        }

        @Test
        void should_handle_multiple_keys_independently() {
            cache.get("a");
            cache.get("b");

            assertThat(executions.get()).isEqualTo(2);
        }

        @Test
        void should_refresh_each_key_independently_after_expiry() {
            cache.get("a");
            cache.get("b");

            instant.updateAndGet(t -> t.plusSeconds(10));

            cache.get("a");
            cache.get("b");

            assertThat(executions.get()).isEqualTo(4);
        }
    }
}
