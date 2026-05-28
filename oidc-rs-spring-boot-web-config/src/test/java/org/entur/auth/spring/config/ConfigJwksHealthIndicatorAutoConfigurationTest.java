package org.entur.auth.spring.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.JWKSetSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import lombok.NonNull;
import lombok.val;
import org.entur.auth.spring.common.health.indicator.jwks.JwksHealthCache;
import org.entur.auth.spring.common.health.indicator.jwks.JwksHealthIndicator;
import org.entur.auth.spring.common.server.JWKSourceWithIssuer;
import org.entur.auth.spring.config.server.IssuerAuthenticationManagerResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfigJwksHealthIndicatorAutoConfiguration test suite")
class ConfigJwksHealthIndicatorAutoConfigurationTest {
    private static final @NonNull String JWKS_CLOCK_QUALIFIER = "jwksClock";
    private static final String JWKS_HEALTH_EXECUTOR_QUALIFIER = "jwksHealthExecutor";
    private static final @NonNull String JWKS_HEALTH_CACHE_QUALIFIER = "jwksHealthCache";
    private static final @NonNull String JWKS_HEALTH_INDICATOR_QUALIFIER = "jwksState";

    @Mock private IssuerAuthenticationManagerResolver resolver;

    @Mock private JWKSourceWithIssuer<?> jwkSource;

    @Mock private JWKSetSource<SecurityContext> jwkSetSource;

    private final @NonNull WebApplicationContextRunner contextRunner =
            new WebApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(ConfigJwksHealthIndicatorAutoConfiguration.class))
                    .withPropertyValues("entur.auth.tenants.environment=mock")
                    .withBean(IssuerAuthenticationManagerResolver.class, () -> resolver)
                    .withInitializer(
                            context ->
                                    context.addBeanFactoryPostProcessor(
                                            beanFactory -> {
                                                beanFactory.addBeanPostProcessor(
                                                        new BeanPostProcessor() {
                                                            @Override
                                                            public @NonNull Object postProcessAfterInitialization(
                                                                    final @NonNull Object bean, final @NonNull String beanName)
                                                                    throws BeansException {
                                                                if (bean instanceof JwksHealthCache) return spy(bean);

                                                                return bean;
                                                            }
                                                        });
                                            }));

    @Test
    void should_create_beans_when_enabled() {
        contextRunner
                .withPropertyValues("management.health.jwks.enabled=true")
                .run(
                        context ->
                                assertThat(context)
                                        .hasSingleBean(JwksHealthCache.class)
                                        .hasSingleBean(JwksHealthIndicator.class));
    }

    @Test
    void should_not_create_beans_when_disabled() {
        contextRunner
                .withPropertyValues("management.health.jwks.enabled=false")
                .run(
                        context ->
                                assertThat(context)
                                        .doesNotHaveBean(JwksHealthCache.class)
                                        .doesNotHaveBean(JwksHealthIndicator.class));
    }

    @Test
    void should_still_create_jwks_clock_when_differently_named_clock_bean_exists() {
        contextRunner
                .withPropertyValues("management.health.jwks.enabled=true")
                .withBean("testClock", Clock.class, Clock::systemDefaultZone)
                .run(
                        context ->
                                assertThat(context)
                                        .hasBean(JWKS_CLOCK_QUALIFIER)
                                        .hasSingleBean(JwksHealthCache.class)
                                        .hasSingleBean(JwksHealthIndicator.class));
    }

    @Test
    void should_still_create_jwks_health_executor_when_differently_named_executor_bean_exists() {
        contextRunner
                .withPropertyValues("management.health.jwks.enabled=true")
                .withBean("testExecutor", ExecutorService.class, Executors::newSingleThreadExecutor)
                .run(
                        context ->
                                assertThat(context)
                                        .hasBean(JWKS_HEALTH_EXECUTOR_QUALIFIER)
                                        .hasSingleBean(JwksHealthCache.class)
                                        .hasSingleBean(JwksHealthIndicator.class));
    }

    @Test
    void should_return_unknown_when_no_jwks_sources_are_empty() {
        when(resolver.getRemoteJWKSets()).thenReturn(List.of());

        contextRunner
                .withPropertyValues("management.health.jwks.enabled=true")
                .run(
                        context -> {
                            val cache = context.getBean(JWKS_HEALTH_CACHE_QUALIFIER, JwksHealthCache.class);

                            val health =
                                    context
                                            .getBean(JWKS_HEALTH_INDICATOR_QUALIFIER, JwksHealthIndicator.class)
                                            .health();

                            assertThat(health).isEqualTo(Health.unknown().build());

                            verifyNoInteractions(cache);
                            verifyNoInteractions(jwkSetSource);
                        });
    }

    @Test
    void should_return_up_when_all_jwks_sources_are_healthy() throws KeySourceException {
        when(resolver.getRemoteJWKSets()).thenReturn(List.of(jwkSource));
        when(jwkSource.getJWKSetSource()).thenReturn(jwkSetSource);
        when(jwkSetSource.getJWKSet(any(), anyLong(), isNull()))
                .thenReturn(new JWKSet(new OctetSequenceKey.Builder(new byte[] {1}).build()));

        contextRunner
                .withPropertyValues("management.health.jwks.enabled=true")
                .run(
                        context -> {
                            val cache = context.getBean(JWKS_HEALTH_CACHE_QUALIFIER, JwksHealthCache.class);

                            val health =
                                    context
                                            .getBean(JWKS_HEALTH_INDICATOR_QUALIFIER, JwksHealthIndicator.class)
                                            .health();

                            assertThat(health).isEqualTo(Health.up().build());

                            verify(cache).get(jwkSource);
                            verify(jwkSetSource).getJWKSet(any(), anyLong(), isNull());
                        });
    }

    @Test
    void should_return_down_when_any_of_jwks_sources_return_empty_jwks() throws KeySourceException {
        when(resolver.getRemoteJWKSets()).thenReturn(List.of(jwkSource));
        when(jwkSource.getJWKSetSource()).thenReturn(jwkSetSource);
        when(jwkSetSource.getJWKSet(any(), anyLong(), isNull())).thenReturn(new JWKSet());

        contextRunner
                .withPropertyValues("management.health.jwks.enabled=true")
                .run(
                        context -> {
                            val cache = context.getBean(JWKS_HEALTH_CACHE_QUALIFIER, JwksHealthCache.class);

                            val health =
                                    context
                                            .getBean(JWKS_HEALTH_INDICATOR_QUALIFIER, JwksHealthIndicator.class)
                                            .health();

                            assertThat(health).isEqualTo(Health.down().build());

                            verify(cache).get(jwkSource);
                            verify(jwkSetSource).getJWKSet(any(), anyLong(), isNull());
                        });
    }

    @Test
    void should_return_down_when_any_of_jwks_sources_throws_exception() throws KeySourceException {
        when(resolver.getRemoteJWKSets()).thenReturn(List.of(jwkSource));
        when(jwkSource.getJWKSetSource()).thenReturn(jwkSetSource);
        when(jwkSetSource.getJWKSet(any(), anyLong(), isNull())).thenThrow(KeySourceException.class);

        contextRunner
                .withPropertyValues("management.health.jwks.enabled=true")
                .run(
                        context -> {
                            val cache = context.getBean(JWKS_HEALTH_CACHE_QUALIFIER, JwksHealthCache.class);

                            val health =
                                    context
                                            .getBean(JWKS_HEALTH_INDICATOR_QUALIFIER, JwksHealthIndicator.class)
                                            .health();

                            assertThat(health).isEqualTo(Health.down().build());

                            verify(cache).get(jwkSource);
                            verify(jwkSetSource).getJWKSet(any(), anyLong(), isNull());
                        });
    }

    @Test
    void should_return_down_when_one_of_multiple_sources_is_unhealthy() throws KeySourceException {
        val healthy = Mockito.<JWKSourceWithIssuer<?>>mock();
        val unhealthy = Mockito.<JWKSourceWithIssuer<?>>mock();

        when(resolver.getRemoteJWKSets()).thenReturn(List.of(healthy, unhealthy));
        when(healthy.getJWKSetSource()).thenReturn(jwkSetSource);
        when(unhealthy.getJWKSetSource()).thenReturn(jwkSetSource);
        when(jwkSetSource.getJWKSet(any(), anyLong(), isNull()))
                .thenReturn(new JWKSet(new OctetSequenceKey.Builder(new byte[] {1}).build()))
                .thenReturn(new JWKSet());

        contextRunner
                .withPropertyValues("management.health.jwks.enabled=true")
                .run(
                        context -> {
                            val cache = context.getBean(JWKS_HEALTH_CACHE_QUALIFIER, JwksHealthCache.class);

                            val health =
                                    context
                                            .getBean(JWKS_HEALTH_INDICATOR_QUALIFIER, JwksHealthIndicator.class)
                                            .health();

                            assertThat(health).isEqualTo(Health.down().build());

                            verify(cache).get(healthy);
                            verify(cache).get(unhealthy);
                            verify(jwkSetSource, times(2)).getJWKSet(any(), anyLong(), isNull());
                        });
    }

    @Test
    void should_throttle_jwks_calls_through_cache() throws KeySourceException {
        when(resolver.getRemoteJWKSets()).thenReturn(List.of(jwkSource));
        when(jwkSource.getJWKSetSource()).thenReturn(jwkSetSource);
        when(jwkSetSource.getJWKSet(any(), anyLong(), isNull()))
                .thenReturn(new JWKSet(new OctetSequenceKey.Builder(new byte[] {1}).build()));

        contextRunner
                .withPropertyValues("management.health.jwks.enabled=true")
                .run(
                        context -> {
                            val cache = context.getBean(JWKS_HEALTH_CACHE_QUALIFIER, JwksHealthCache.class);

                            val indicator =
                                    context.getBean(JWKS_HEALTH_INDICATOR_QUALIFIER, JwksHealthIndicator.class);

                            indicator.health();
                            indicator.health();
                            indicator.health();

                            verify(cache, times(3)).get(jwkSource);
                            verify(jwkSetSource).getJWKSet(any(), anyLong(), isNull());
                        });
    }

    @Test
    void should_refresh_after_ttl_expires() throws Exception {
        val instant = new AtomicReference<>(Instant.now());

        val clock =
                new Clock() {
                    @Override
                    public ZoneId getZone() {
                        return null;
                    }

                    @Override
                    public Clock withZone(ZoneId zone) {
                        return this;
                    }

                    @Override
                    public Instant instant() {
                        return instant.get();
                    }
                };

        when(resolver.getRemoteJWKSets()).thenReturn(List.of(jwkSource));
        when(jwkSource.getJWKSetSource()).thenReturn(jwkSetSource);

        when(jwkSetSource.getJWKSet(any(), anyLong(), isNull()))
                .thenReturn(new JWKSet(new OctetSequenceKey.Builder(new byte[] {1}).build()));

        contextRunner
                .withPropertyValues("management.health.jwks.enabled=true")
                .withBean(JWKS_CLOCK_QUALIFIER, Clock.class, () -> clock)
                .run(
                        context -> {
                            val cache = context.getBean(JWKS_HEALTH_CACHE_QUALIFIER, JwksHealthCache.class);

                            val indicator =
                                    context.getBean(JWKS_HEALTH_INDICATOR_QUALIFIER, JwksHealthIndicator.class);

                            indicator.health();

                            instant.updateAndGet(t -> t.plusSeconds(10));

                            indicator.health();

                            verify(cache, times(2)).get(jwkSource);
                            verify(jwkSetSource, times(2)).getJWKSet(any(), anyLong(), isNull());
                        });
    }
}
