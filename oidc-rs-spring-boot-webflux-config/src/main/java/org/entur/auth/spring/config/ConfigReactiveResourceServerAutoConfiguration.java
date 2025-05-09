package org.entur.auth.spring.config;

import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.common.authorization.AuthorizationProperties;
import org.entur.auth.spring.common.cors.CorsCondition;
import org.entur.auth.spring.common.cors.CorsProperties;
import org.entur.auth.spring.common.mdc.MdcProperties;
import org.entur.auth.spring.common.server.ServerCondition;
import org.entur.auth.spring.config.authorization.ReactiveAuthorizationHelper;
import org.entur.auth.spring.config.cors.ReactiveCorsHelper;
import org.entur.auth.spring.config.mdc.ReactiveMdcRequestFilter;
import org.entur.auth.spring.webflux.autorization.ReactiveConfigureAuthorizeExchange;
import org.entur.auth.spring.webflux.cors.ReactiveConfigureCors;
import org.entur.auth.spring.webflux.mdc.ReactiveConfigureMdcRequestFilter;
import org.entur.auth.spring.webflux.server.ReactiveConfigureAuth2ResourceServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
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
@EnableConfigurationProperties({
    MdcProperties.class,
    CorsProperties.class,
    AuthorizationProperties.class
})
public class ConfigReactiveResourceServerAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "entur.auth.authorization",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    ReactiveConfigureAuthorizeExchange reactiveConfigureAuthorizeExchange(
            AuthorizationProperties authorizationProperties,
            @Value("${management.endpoints.web.base-path:/actuator}") String managementBasePath) {
        log.debug("Configure Reactive AuthorizeExchange");
        return customizer ->
                ReactiveAuthorizationHelper.configure(
                        customizer, authorizationProperties, managementBasePath);
    }

    @Bean
    @Conditional(CorsCondition.class)
    ReactiveConfigureCors reactiveConfigureCors(CorsProperties corsProperties) {
        log.debug("Configure Reactive Cors");
        return configurer -> ReactiveCorsHelper.configure(configurer, corsProperties);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "entur.auth.mdc",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    ReactiveConfigureMdcRequestFilter reactiveConfigureMdcRequestFilter(MdcProperties mdcProperties) {
        log.debug("Configure Reactive MDC");
        return new ReactiveMdcRequestFilter(mdcProperties);
    }

    @Bean
    @Conditional(ServerCondition.class)
    public ReactiveConfigureAuth2ResourceServer reactiveConfigureAuth2ResourceServer(
            ReactiveAuthenticationManagerResolver<ServerWebExchange> authenticationManagerResolver) {
        log.debug("Configure Reactive ResourceServer");
        return configurer -> configurer.authenticationManagerResolver(authenticationManagerResolver);
    }
}
