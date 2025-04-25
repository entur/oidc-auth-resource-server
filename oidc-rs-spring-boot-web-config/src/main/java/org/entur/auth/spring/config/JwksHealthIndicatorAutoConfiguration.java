package org.entur.auth.spring.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.entur.auth.spring.config.server.ServerCondition;
import org.entur.auth.spring.config.server.SupportsReadiness;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;

/**
 * Configuration class for a custom health indicator related to JWKS (JSON Web Key Set).
 */
@Configuration("jwksState")
@Conditional(ServerCondition.class)
@ConditionalOnClass(HealthIndicator.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnEnabledHealthIndicator("jwks")
@RequiredArgsConstructor
public class JwksHealthIndicatorAutoConfiguration implements HealthIndicator {
   private final AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

    /**
     * Implementation of the HealthIndicator interface. Checks the health status related to JWKS.
     *
     * @return A Health object representing the health status.
     */
    @Override
    public Health health() {
        final Health.Builder status;

        if (authenticationManagerResolver instanceof SupportsReadiness supportsReadiness) {
            status = supportsReadiness.getReadiness() ? Health.up() : Health.down();
        } else {
            status = Health.up();
        }

        return status.build();
    }
}
