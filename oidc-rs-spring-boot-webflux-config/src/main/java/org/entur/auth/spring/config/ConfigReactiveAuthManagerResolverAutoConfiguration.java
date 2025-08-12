package org.entur.auth.spring.config;

import com.nimbusds.jose.jwk.source.JWKSetSourceWithHealthStatusReporting;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.health.HealthReportListener;
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
import org.entur.auth.spring.config.server.ReactiveIssuerAuthenticationManagerResolver;
import org.entur.auth.spring.config.server.ReactiveJWKSourceWithIssuer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnMissingBean(ReactiveAuthenticationManagerResolver.class)
@Conditional(ServerCondition.class)
@AutoConfigureAfter(ConfigReactiveExternalPropertyAutoConfiguration.class)
@EnableConfigurationProperties({EnturAuthProperties.class})
@RequiredArgsConstructor
public class ConfigReactiveAuthManagerResolverAutoConfiguration {
    private final EnturAuthProperties enturAuthProperties;
    private final AuthProviders authProviders;
    private final ObjectProvider<
                    HealthReportListener<
                            JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext>>
            healthReportListener;

    Map<String, ReactiveAuthenticationManager> authenticationManagers = new HashMap<>();
    List<ReactiveJWKSourceWithIssuer> remoteJWKSets = new ArrayList<>();

    @Bean
    public ReactiveAuthenticationManagerResolver<ServerWebExchange>
            reactiveAuthenticationManagerResolver() {
        log.debug("Configure AuthenticationManagerResolver");
        var authoritiesConverter = new TenantJwtGrantedAuthoritiesConverter(authProviders);

        var tenantsProperties = enturAuthProperties.getTenants();
        var issuerProperties = enturAuthProperties.getIssuers();
        var externalProperties = enturAuthProperties.getExternal();

        if (tenantsProperties.getEnvironment() != null || tenantsProperties.getInclude() != null) {
            log.info("Tenant environment = {}", tenantsProperties.getEnvironment());
            log.info("Tenant include = {}", tenantsProperties.getInclude());
        }

        var listner = healthReportListener.getIfAvailable();
        if (listner == null) {
            log.info("HealthReportListener not configured");
        }

        var environmentIssuerProperties =
                authProviders.get(tenantsProperties.getEnvironment(), tenantsProperties.getInclude());

        var managerResolver =
                new ReactiveIssuerAuthenticationManagerResolver(
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
