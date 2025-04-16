package org.entur.auth.spring.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.config.authorization.AuthorizationHelper;
import org.entur.auth.spring.config.authorization.AuthorizationProperties;
import org.entur.auth.spring.config.cors.CorsCondition;
import org.entur.auth.spring.config.cors.CorsHelper;
import org.entur.auth.spring.config.cors.CorsProperties;
import org.entur.auth.spring.config.mdc.MdcProperties;
import org.entur.auth.spring.config.mdc.MdcRequestFilter;
import org.entur.auth.spring.config.server.IssuersProperties;
import org.entur.auth.spring.config.server.ServerCondition;
import org.entur.auth.spring.config.server.ServerHelper;
import org.entur.auth.spring.config.server.TenantsProperties;
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

/**
 * Configuration of OAuth 2.0 Resource Server JWT
 * @see "https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html"
 */

@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ResourceServerAutoConfiguration {


    @ConditionalOnProperty(prefix = "entur.auth.authorization", name = "enabled", havingValue = "true", matchIfMissing = true)
    @EnableConfigurationProperties(AuthorizationProperties.class)
    @RequiredArgsConstructor
    public static class ConfigureAuthorizeHttpRequestsBean {
        private final AuthorizationProperties authorizationProperties;
        @Value("${management.endpoints.web.base-path:/actuator}") String managementBasePath;

        @Bean
        ConfigureAuthorizeRequests configureAuthorizeRequests() {
            log.info("Configure Authorize Requests");
            return registry -> AuthorizationHelper.configure(registry, authorizationProperties, managementBasePath);
        }
    }

    @Conditional(CorsCondition.class)
    @ConditionalOnProperty(prefix = "entur.auth.cors", name = "enabled", havingValue = "true", matchIfMissing = true)
    @EnableConfigurationProperties(CorsProperties.class)
    @RequiredArgsConstructor
    static class ConfigureCorsBean {
        private final CorsProperties corsProperties;

        @Bean
        ConfigureCors configureCors() {
            log.info("Configure CORS");
            return configurer -> CorsHelper.configure(configurer, corsProperties);
        }
    }

    @ConditionalOnProperty(prefix = "entur.auth.mdc", name = "enabled", havingValue = "true", matchIfMissing = true)
    @EnableConfigurationProperties(MdcProperties.class)
    @RequiredArgsConstructor
    static class ConfigureMdcRequestFilterBean {
        private final MdcProperties mdcProperties;

        @Bean
        ConfigureMdcRequestFilter configureMdcRequestFilter() {
            log.info("Configure MDC");
            return new MdcRequestFilter(mdcProperties);
        }
    }

    @Conditional(ServerCondition.class)
    @EnableConfigurationProperties({TenantsProperties.class, IssuersProperties.class})
    @RequiredArgsConstructor
    public static class ConfigureAuth2ResourceServerBean {
        private final TenantsProperties tenantsProperties;
        private final IssuersProperties issuerProperties;

        @Bean
        public ConfigureAuth2ResourceServer configureAuth2ResourceServer() {
            return configurer -> ServerHelper.configure(configurer, tenantsProperties, issuerProperties);
        }
    }
}