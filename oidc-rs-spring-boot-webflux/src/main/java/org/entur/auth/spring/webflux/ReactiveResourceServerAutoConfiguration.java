package org.entur.auth.spring.webflux;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.webflux.mdc.ReactiveConfigureMdcRequestFilter;
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
    private final ReactiveConfigureSessionManagement reactiveConfigureSessionManagement;
    private final ServerHttpSecurity http;

    /**
     * Spring Security 7 introduced a new concept of top level HttpSecurity Customizer beans, where
     * Spring Security will automatically apply any top level HttpSecurity Customizer beans.
     *
     * <p>A top level HttpSecurity Customizer type can be summarized as any Customizer that matches
     * public {@code HttpSecurity.*(Customizer)}. This translates to any Customizer that is a single
     * argument to a public method on HttpSecurity.
     *
     * @see <a
     *     href="https://docs.spring.io/spring-security/reference/reactive/configuration/webflux.html#top-level-customizer-bean">Top
     *     Level HttpSecurity Customizer Beans</a>
     * @see <a
     *     href="https://github.com/spring-projects/spring-security/commit/a8f045eb50ef975a02df8c3699bce9390d9c21bb">Changes
     *     introduced in Spring Security 7</a>
     */
    @Bean
    public SecurityWebFilterChain filterChain() throws ReactiveResourceServerConfigurationException {
        try {
            return http.requestCache(reactiveConfigureSessionManagement)
                    .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                    .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                    .logout(ServerHttpSecurity.LogoutSpec::disable)
                    .addFilterBefore(reactiveConfigureMdcRequestFilter, SecurityWebFiltersOrder.AUTHORIZATION)
                    .build();
        } catch (Exception e) {
            throw new ReactiveResourceServerConfigurationException(e);
        }
    }
}
