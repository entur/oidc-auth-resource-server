package org.entur.auth.spring.web;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.web.authorization.ConfigureAuthorizeRequests;
import org.entur.auth.spring.web.cors.ConfigureCors;
import org.entur.auth.spring.web.mdc.ConfigureMdcRequestFilter;
import org.entur.auth.spring.web.server.ConfigureAuth2ResourceServer;
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
    private final ConfigureAuthorizeRequests configureAuthorizeRequests;
    private final ConfigureAuth2ResourceServer configureAuth2ResourceServer;
    private final ConfigureCors configureCors;
    private final HttpSecurity http;

    @Bean
    public SecurityFilterChain filterChain() throws ResourceServerConfigurationException {
        try {
            return http.sessionManagement(
                            sessionManager -> sessionManager.sessionCreationPolicy(STATELESS))
                    .csrf(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .logout(AbstractHttpConfigurer::disable)
                    .cors(configureCors)
                    .addFilterBefore(configureMdcRequestFilter, AuthorizationFilter.class)
                    .authorizeHttpRequests(configureAuthorizeRequests)
                    .oauth2ResourceServer(configureAuth2ResourceServer)
                    .build();
        } catch (Exception e) {
            throw new ResourceServerConfigurationException(e);
        }
    }
}
