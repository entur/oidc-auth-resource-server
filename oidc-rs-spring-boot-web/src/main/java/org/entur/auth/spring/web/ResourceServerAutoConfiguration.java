package org.entur.auth.spring.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.web.mdc.ConfigureMdcRequestFilter;
import org.entur.auth.spring.web.session.ConfigureSessionManagement;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

/**
 * Configuration of OAuth 2.0 Resource Server JWT
 *
 * @see "https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html"
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnMissingBean(SecurityFilterChain.class)
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class ResourceServerAutoConfiguration {
    private final ConfigureMdcRequestFilter configureMdcRequestFilter;
    private final ConfigureSessionManagement configureSessionManagement;
    private final HttpSecurity http;

    /**
     * Spring Security 7 introduced a new concept of top level HttpSecurity Customizer beans, where
     * Spring Security will automatically apply any top level HttpSecurity Customizer beans.
     *
     * <p>A top level HttpSecurity Customizer type can be summarized as any Customizer that matches
     * public {@code HttpSecurity.*(Customizer)}. This translates to any Customizer that is a single
     * argument to a public method on HttpSecurity.
     *
     * @see <a
     *     href="https://docs.spring.io/spring-security/reference/servlet/configuration/java.html#top-level-customizer-bean">Top
     *     Level HttpSecurity Customizer Beans</a>
     * @see <a
     *     href="https://github.com/spring-projects/spring-security/commit/a8f045eb50ef975a02df8c3699bce9390d9c21bb">Changes
     *     introduced in Spring Security 7</a>
     */
    @Bean
    public SecurityFilterChain filterChain() throws ResourceServerConfigurationException {
        try {
            return http.sessionManagement(configureSessionManagement)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .logout(AbstractHttpConfigurer::disable)
                    .addFilterBefore(configureMdcRequestFilter, AuthorizationFilter.class)
                    .build();
        } catch (Exception e) {
            throw new ResourceServerConfigurationException(e);
        }
    }
}
