package org.entur.auth.spring.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.common.authorization.AuthorizationProperties;
import org.entur.auth.spring.common.cors.CorsCondition;
import org.entur.auth.spring.common.cors.CorsProperties;
import org.entur.auth.spring.common.mdc.MdcProperties;
import org.entur.auth.spring.common.server.ServerCondition;
import org.entur.auth.spring.config.authorization.AuthorizationHelper;
import org.entur.auth.spring.config.cors.CorsHelper;
import org.entur.auth.spring.config.mdc.MdcRequestFilter;
import org.entur.auth.spring.web.authorization.ConfigureAuthorizeRequests;
import org.entur.auth.spring.web.cors.ConfigureCors;
import org.entur.auth.spring.web.mdc.ConfigureMdcRequestFilter;
import org.entur.auth.spring.web.server.ConfigureAuth2ResourceServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManagerResolver;

/**
 * Configuration of OAuth 2.0 Resource Server JWT
 *
 * @see "https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html"
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties({
    MdcProperties.class,
    CorsProperties.class,
    AuthorizationProperties.class
})
public class ConfigResourceServerAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "entur.auth.authorization",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    ConfigureAuthorizeRequests configureAuthorizeRequests(
            AuthorizationProperties authorizationProperties,
            @Value("${management.endpoints.web.base-path:/actuator}") String managementBasePath) {
        log.debug("Configure Authorize Requests");
        return registry ->
                AuthorizationHelper.configure(registry, authorizationProperties, managementBasePath);
    }

    @Bean
    @Conditional(CorsCondition.class)
    ConfigureCors configureCors(CorsProperties corsProperties) {
        log.debug("Configure CORS");
        return configurer -> CorsHelper.configure(configurer, corsProperties);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "entur.auth.mdc",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    ConfigureMdcRequestFilter configureMdcRequestFilter(MdcProperties mdcProperties) {
        log.debug("Configure MDC");
        return new MdcRequestFilter(mdcProperties);
    }

    @Bean
    @Conditional(ServerCondition.class)
    public ConfigureAuth2ResourceServer configureAuth2ResourceServer(
            AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver) {
        log.debug("Configure ResourceServer");

        return configurer -> configurer.authenticationManagerResolver(authenticationManagerResolver);
    }
}
