package org.entur.auth.spring.webflux;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.webflux.autorization.ReactiveConfigureAuthorizeExchange;
import org.entur.auth.spring.webflux.autorization.ReactiveDefaultConfigureAuthorizeExchange;
import org.entur.auth.spring.webflux.cors.ReactiveConfigureCors;
import org.entur.auth.spring.webflux.cors.ReactiveDefaultConfigureCors;
import org.entur.auth.spring.webflux.mdc.ReactiveConfigureMdcRequestFilter;
import org.entur.auth.spring.webflux.mdc.ReactiveDefaultMdcRequestFilter;
import org.entur.auth.spring.webflux.server.ReactiveConfigureAuth2ResourceServer;
import org.entur.auth.spring.webflux.server.ReactiveDefaultConfigureAuth2ResourceServer;
import org.entur.auth.spring.webflux.user.ReactiveNoUserDetailsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

/**
 * Configuration of OAuth 2.0 Resource Server JWT
 *
 * @see
 *     "https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/index.html"
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveResourceServerAutoConfiguration {

    @EnableWebFluxSecurity
    @EnableReactiveMethodSecurity
    @RequiredArgsConstructor
    @ConditionalOnMissingBean(SecurityWebFilterChain.class)
    public static class SecurityFilterChainBean {
        private final ReactiveConfigureMdcRequestFilter reactiveConfigureMdcRequestFilter;
        private final ReactiveConfigureAuthorizeExchange reactiveConfigureAuthorizeExchange;
        private final ReactiveConfigureAuth2ResourceServer reactiveConfigureAuth2ResourceServer;
        private final ReactiveConfigureCors reactiveConfigureCors;

        @Bean
        public SecurityWebFilterChain filterChain(ServerHttpSecurity http)
                throws ReactiveResourceServerConfigurationException {
            try {
                return http.requestCache(
                                requestCache ->
                                        requestCache.requestCache(
                                                NoOpServerRequestCache
                                                        .getInstance())) // Disable WebSession read on every request
                        .csrf(ServerHttpSecurity.CsrfSpec::disable)
                        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                        .logout(ServerHttpSecurity.LogoutSpec::disable)
                        .cors(reactiveConfigureCors)
                        .addFilterBefore(
                                reactiveConfigureMdcRequestFilter, SecurityWebFiltersOrder.AUTHORIZATION)
                        .authorizeExchange(reactiveConfigureAuthorizeExchange)
                        .oauth2ResourceServer(reactiveConfigureAuth2ResourceServer)
                        .build();
            } catch (Exception e) {
                throw new ReactiveResourceServerConfigurationException(e);
            }
        }
    }

    @ConditionalOnMissingBean(ReactiveUserDetailsService.class)
    public static class ReactiveUserDetailsServiceBean {
        @Bean
        public ReactiveUserDetailsService userDetailsService() {
            return new ReactiveNoUserDetailsService(); // avoid the default user.
        }
    }

    @ConditionalOnMissingBean(ReactiveConfigureAuthorizeExchange.class)
    public static class ReactiveConfigureAuthorizeExchangeBean {
        @Bean
        public ReactiveConfigureAuthorizeExchange reactiveConfigureAuthorizeExchange() {
            return new ReactiveDefaultConfigureAuthorizeExchange();
        }
    }

    @ConditionalOnMissingBean(ReactiveConfigureCors.class)
    public static class ReactiveConfigureCorsBean {
        @Bean
        public ReactiveConfigureCors reactiveConfigureCors() {
            return new ReactiveDefaultConfigureCors();
        }
    }

    @ConditionalOnMissingBean(ReactiveConfigureMdcRequestFilter.class)
    public static class ReactiveConfigureMdcRequestFilterBean {
        @Bean
        public ReactiveConfigureMdcRequestFilter reactiveConfigureMdcRequestFilter() {
            return new ReactiveDefaultMdcRequestFilter();
        }
    }

    @ConditionalOnMissingBean(ReactiveConfigureAuth2ResourceServer.class)
    public static class ReactiveConfigureAuth2ResourceServerBean {
        @Bean
        public ReactiveConfigureAuth2ResourceServer reactiveConfigureAuth2ResourceServer() {
            return new ReactiveDefaultConfigureAuth2ResourceServer();
        }
    }
}
