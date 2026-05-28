package org.entur.auth.spring.config;

import static java.time.Clock.systemDefaultZone;
import static java.time.Duration.ofSeconds;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

import java.time.Clock;
import java.util.concurrent.ExecutorService;
import lombok.NonNull;
import org.entur.auth.spring.common.health.indicator.jwks.JwksHealthCache;
import org.entur.auth.spring.common.health.indicator.jwks.JwksHealthIndicator;
import org.entur.auth.spring.common.server.ServerCondition;
import org.entur.auth.spring.config.server.IssuerAuthenticationManagerResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

/** Configuration class for a custom health indicator related to JWKS (JSON Web Key Set). */
@Configuration
@Conditional(ServerCondition.class)
@ConditionalOnClass(HealthIndicator.class)
@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnEnabledHealthIndicator("jwks")
public class ConfigJwksHealthIndicatorAutoConfiguration {
    private static final String JWKS_CLOCK_QUALIFIER = "jwksClock";
    private static final String JWKS_HEALTH_EXECUTOR_QUALIFIER = "jwksHealthExecutor";

    @Bean
    @ConditionalOnMissingBean(name = JWKS_CLOCK_QUALIFIER)
    public @NonNull Clock jwksClock() {
        return systemDefaultZone();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = JWKS_HEALTH_EXECUTOR_QUALIFIER)
    public @NonNull ExecutorService jwksHealthExecutor() {
        return newSingleThreadExecutor(new CustomizableThreadFactory("jwks-health-"));
    }

    @Bean
    @ConditionalOnMissingBean
    public @NonNull JwksHealthCache jwksHealthCache(
            final @NonNull @Qualifier(JWKS_CLOCK_QUALIFIER) Clock clock,
            final @NonNull @Qualifier(JWKS_HEALTH_EXECUTOR_QUALIFIER) ExecutorService executor) {
        return new JwksHealthCache(clock, executor, ofSeconds(5));
    }

    @Bean("jwksState")
    public @NonNull JwksHealthIndicator jwksHealthIndicator(
            final @NonNull IssuerAuthenticationManagerResolver resolver,
            final @NonNull JwksHealthCache cache) {
        return new JwksHealthIndicator(resolver.getRemoteJWKSets(), cache);
    }
}
