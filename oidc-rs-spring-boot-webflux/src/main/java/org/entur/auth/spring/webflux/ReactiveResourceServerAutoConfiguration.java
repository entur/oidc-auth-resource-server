package org.entur.auth.spring.webflux;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.webflux.autorization.ReactiveConfigureAuthorizeExchange;
import org.entur.auth.spring.webflux.cors.ReactiveConfigureCors;
import org.entur.auth.spring.webflux.csrf.ReactiveConfigureCsrf;
import org.entur.auth.spring.webflux.mdc.ReactiveConfigureMdcRequestFilter;
import org.entur.auth.spring.webflux.server.ReactiveConfigureAuth2ResourceServer;
import org.entur.auth.spring.webflux.sesssion.ReactiveConfigureSessionManagement;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuration of OAuth 2.0 Resource Server JWT
 *
 * @see
 *     "https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/index.html"
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnMissingBean(SecurityWebFilterChain.class)
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class ReactiveResourceServerAutoConfiguration {
    private final ReactiveConfigureMdcRequestFilter reactiveConfigureMdcRequestFilter;
    private final ReactiveConfigureAuthorizeExchange reactiveConfigureAuthorizeExchange;
    private final ReactiveConfigureAuth2ResourceServer reactiveConfigureAuth2ResourceServer;
    private final ReactiveConfigureSessionManagement reactiveConfigureSessionManagement;
    private final ReactiveConfigureCsrf reactiveConfigureCsrf;
    private final ReactiveConfigureCors reactiveConfigureCors;
    private final ServerHttpSecurity http;

    @Bean
    public SecurityWebFilterChain filterChain() throws ReactiveResourceServerConfigurationException {
        try {
            return http.requestCache(reactiveConfigureSessionManagement)
                    .csrf(reactiveConfigureCsrf)
                    .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                    .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                    .logout(ServerHttpSecurity.LogoutSpec::disable)
                    .cors(reactiveConfigureCors)
                    .addFilterBefore(reactiveConfigureMdcRequestFilter, SecurityWebFiltersOrder.AUTHORIZATION)
                    .authorizeExchange(reactiveConfigureAuthorizeExchange)
                    .oauth2ResourceServer(reactiveConfigureAuth2ResourceServer)
                    .build();
        } catch (Exception e) {
            throw new ReactiveResourceServerConfigurationException(e);
        }
    }
}
