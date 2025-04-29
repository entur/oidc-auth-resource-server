package org.entur.auth.spring.config;

import com.nimbusds.jose.jwk.source.JWKSetSourceWithHealthStatusReporting;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.health.HealthReportListener;
import com.nimbusds.jose.util.health.HealthStatus;
import lombok.RequiredArgsConstructor;
import org.entur.auth.spring.config.server.ServerCondition;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/** Configuration class for a custom health indicator related to JWKS (JSON Web Key Set). */
@Configuration("jwksState")
@Conditional(ServerCondition.class)
@ConditionalOnClass(HealthIndicator.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnEnabledHealthIndicator("jwks")
@RequiredArgsConstructor
public class JwksHealthIndicatorAutoConfiguration implements HealthIndicator {
    final Health.Builder status = Health.up();

    /**
     * Implementation of the HealthIndicator interface. Checks the health status related to JWKS.
     *
     * @return A Health object representing the health status.
     */
    @Override
    public Health health() {
        return status.build();
    }

    @Bean
    HealthReportListener<JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext>
            healthReportListener() {
        return healthReport ->
                status.status(
                        healthReport.getHealthStatus() == HealthStatus.NOT_HEALTHY ? Status.DOWN : Status.UP);
    }
}
