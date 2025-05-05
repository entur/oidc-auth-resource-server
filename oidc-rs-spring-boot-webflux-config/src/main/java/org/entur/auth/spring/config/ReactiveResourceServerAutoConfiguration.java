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
import org.entur.auth.spring.common.authorization.AuthorizationProperties;
import org.entur.auth.spring.common.cors.CorsCondition;
import org.entur.auth.spring.common.cors.CorsProperties;
import org.entur.auth.spring.common.mdc.MdcProperties;
import org.entur.auth.spring.common.server.AuthProviders;
import org.entur.auth.spring.common.server.DefaultAuthProviders;
import org.entur.auth.spring.common.server.EnturAuthProperties;
import org.entur.auth.spring.common.server.ServerCondition;
import org.entur.auth.spring.common.server.TenantJwtGrantedAuthoritiesConverter;
import org.entur.auth.spring.config.authorization.ReactiveAuthorizationHelper;
import org.entur.auth.spring.config.cors.ReactiveCorsHelper;
import org.entur.auth.spring.config.mdc.ReactiveMdcRequestFilter;
import org.entur.auth.spring.config.server.ReactiveIssuerAuthenticationManagerResolver;
import org.entur.auth.spring.config.server.ReactiveJWKSourceWithIssuer;
import org.entur.auth.spring.webflux.autorization.ReactiveConfigureAuthorizeExchange;
import org.entur.auth.spring.webflux.cors.ReactiveConfigureCors;
import org.entur.auth.spring.webflux.mdc.ReactiveConfigureMdcRequestFilter;
import org.entur.auth.spring.webflux.server.ReactiveConfigureAuth2ResourceServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.web.server.ServerWebExchange;

/**
 * Configuration of OAuth 2.0 Resource Server JWT
 *
 * @see "https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html"
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveResourceServerAutoConfiguration {
    @ConditionalOnProperty(
            prefix = "entur.auth.authorization",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @EnableConfigurationProperties(AuthorizationProperties.class)
    @RequiredArgsConstructor
    public static class ReactiveConfigureAuthorizeExchangeBean {
        private final AuthorizationProperties authorizationProperties;

        @Value("${management.endpoints.web.base-path:/actuator}")
        String managementBasePath;

        @Bean
        ReactiveConfigureAuthorizeExchange reactiveConfigureAuthorizeExchange() {
            log.info("Configure Reactive AuthorizeExchange");
            return customizer ->
                    ReactiveAuthorizationHelper.configure(
                            customizer, authorizationProperties, managementBasePath);
        }
    }

    @Conditional(CorsCondition.class)
    @ConditionalOnProperty(
            prefix = "entur.auth.cors",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @EnableConfigurationProperties(CorsProperties.class)
    @RequiredArgsConstructor
    static class ReactiveConfigureCorsBean {
        private final CorsProperties corsProperties;

        @Bean
        ReactiveConfigureCors reactiveConfigureCors() {
            log.info("Configure Reactive Cors");
            return configurer -> ReactiveCorsHelper.configure(configurer, corsProperties);
        }
    }

    @ConditionalOnProperty(
            prefix = "entur.auth.mdc",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    @EnableConfigurationProperties(MdcProperties.class)
    @RequiredArgsConstructor
    static class ReactiveConfigureMdcRequestFilterBean {
        private final MdcProperties mdcProperties;

        @Bean
        ReactiveConfigureMdcRequestFilter reactiveConfigureMdcRequestFilter() {
            log.info("Configure Reactive MDC");
            return new ReactiveMdcRequestFilter(mdcProperties);
        }
    }

    @Conditional(ServerCondition.class)
    @RequiredArgsConstructor
    public static class ConfigureAuth2ResourceServerBean {
        private final ReactiveAuthenticationManagerResolver<ServerWebExchange>
                authenticationManagerResolver;

        @Bean
        public ReactiveConfigureAuth2ResourceServer reactiveConfigureAuth2ResourceServer() {
            log.info("Configure Reactive ResourceServer");
            return configurer -> configurer.authenticationManagerResolver(authenticationManagerResolver);
        }
    }

    @Conditional(ServerCondition.class)
    @RequiredArgsConstructor
    @ConditionalOnMissingBean(AuthProviders.class)
    public static class AuthProvidersBean {

        @Bean
        public AuthProviders authProviders() {
            log.info("Configure DefaultAuthProviders");
            return new DefaultAuthProviders();
        }
    }

    @Conditional(ServerCondition.class)
    @ConditionalOnMissingBean(ReactiveAuthenticationManagerResolver.class)
    @EnableConfigurationProperties({EnturAuthProperties.class})
    @RequiredArgsConstructor
    public static class ReactiveAuthenticationManagerResolverBean {
        private final EnturAuthProperties enturAuthProperties;
        private final AuthProviders authProviders;
        Map<String, ReactiveAuthenticationManager> authenticationManagers = new HashMap<>();
        List<ReactiveJWKSourceWithIssuer> remoteJWKSets = new ArrayList<>();

        @Autowired
        private HealthReportListener<
                        JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext>
                healthReportListener;

        @Bean
        public ReactiveAuthenticationManagerResolver<ServerWebExchange>
                reactiveAuthenticationManagerResolver() {
            log.info("Configure AuthenticationManagerResolver");
            var authoritiesConverter = new TenantJwtGrantedAuthoritiesConverter(authProviders);

            var tenantsProperties = enturAuthProperties.getTenants();
            var issuerProperties = enturAuthProperties.getIssuers();

            if (tenantsProperties.getEnvironment() != null || tenantsProperties.getInclude() != null) {
                log.info("Tenant environment = {}", tenantsProperties.getEnvironment());
                log.info("Tenant include = {}", tenantsProperties.getInclude());
            }

            var environmentIssuerProperties =
                    authProviders.get(tenantsProperties.getEnvironment(), tenantsProperties.getInclude());

            var managerResolver =
                    new ReactiveIssuerAuthenticationManagerResolver(
                            authenticationManagers,
                            remoteJWKSets,
                            enturAuthProperties,
                            authoritiesConverter,
                            healthReportListener);
            environmentIssuerProperties.forEach(managerResolver::addIsser);
            issuerProperties.forEach(managerResolver::addIsser);

            return managerResolver;
        }
    }
}
