package org.entur.auth.spring.config;

import com.nimbusds.jose.jwk.source.JWKSetSourceWithHealthStatusReporting;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.health.HealthReportListener;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.common.server.AuthProviders;
import org.entur.auth.spring.common.server.EnturAuthProperties;
import org.entur.auth.spring.common.server.ServerCondition;
import org.entur.auth.spring.common.server.TenantJwtGrantedAuthoritiesConverter;
import org.entur.auth.spring.config.server.IssuerAuthenticationManagerResolver;
import org.entur.auth.spring.config.server.JWKSourceWithIssuer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;

@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Conditional(ServerCondition.class)
@AutoConfigureAfter(ConfigExternalPropertyAutoConfiguration.class)
@ConditionalOnMissingBean(AuthenticationManagerResolver.class)
@EnableConfigurationProperties({EnturAuthProperties.class})
@RequiredArgsConstructor
public class ConfigAuthManagerResolverAutoConfiguration {
    private final Map<String, AuthenticationManager> authenticationManagers = new HashMap<>();
    private final List<JWKSourceWithIssuer> remoteJWKSets = new ArrayList<>();

    private final EnturAuthProperties enturAuthProperties;
    private final AuthProviders authProviders;
    private final ObjectProvider<
                    HealthReportListener<
                            JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext>>
            healthReportListener;

    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver() {
        log.debug("Configure AuthenticationManagerResolver");
        final var authoritiesConverter = new TenantJwtGrantedAuthoritiesConverter(authProviders);

        final var tenantsProperties = enturAuthProperties.getTenants();
        final var issuerProperties = enturAuthProperties.getIssuers();
        final var externalProperties = enturAuthProperties.getExternal();

        if (tenantsProperties.getEnvironment() != null || tenantsProperties.getInclude() != null) {
            log.info("Tenant environment = {}", tenantsProperties.getEnvironment());
            log.info("Tenant include = {}", tenantsProperties.getInclude());
        }

        final var listner = healthReportListener.getIfAvailable();
        if (listner == null) {
            log.info("HealthReportListener not configured");
        }

        final var environmentIssuerProperties =
                authProviders.get(tenantsProperties.getEnvironment(), tenantsProperties.getInclude());

        final var managerResolver =
                new IssuerAuthenticationManagerResolver(
                        authenticationManagers,
                        remoteJWKSets,
                        enturAuthProperties,
                        authoritiesConverter,
                        listner);
        environmentIssuerProperties.forEach(managerResolver::addIssuer);
        issuerProperties.forEach(managerResolver::addIssuer);
        externalProperties.getFilteredIssuers().forEach(managerResolver::addIssuer);

        return managerResolver;
    }
}
