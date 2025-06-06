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
    Map<String, AuthenticationManager> authenticationManagers = new HashMap<>();
    List<JWKSourceWithIssuer> remoteJWKSets = new ArrayList<>();

    private final EnturAuthProperties enturAuthProperties;
    private final AuthProviders authProviders;
    private final HealthReportListener<
                    JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext>
            healthReportListener;

    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver() {
        log.debug("Configure AuthenticationManagerResolver");
        var authoritiesConverter = new TenantJwtGrantedAuthoritiesConverter(authProviders);

        var tenantsProperties = enturAuthProperties.getTenants();
        var issuerProperties = enturAuthProperties.getIssuers();
        var externalProperties = enturAuthProperties.getExternal();

        if (tenantsProperties.getEnvironment() != null || tenantsProperties.getInclude() != null) {
            log.info("Tenant environment = {}", tenantsProperties.getEnvironment());
            log.info("Tenant include = {}", tenantsProperties.getInclude());
        }

        var environmentIssuerProperties =
                authProviders.get(tenantsProperties.getEnvironment(), tenantsProperties.getInclude());

        var managerResolver =
                new IssuerAuthenticationManagerResolver(
                        authenticationManagers,
                        remoteJWKSets,
                        enturAuthProperties,
                        authoritiesConverter,
                        healthReportListener);
        environmentIssuerProperties.forEach(managerResolver::addIssuer);
        issuerProperties.forEach(managerResolver::addIssuer);
        externalProperties.getFilteredIssuers().forEach(managerResolver::addIssuer);

        return managerResolver;
    }
}
