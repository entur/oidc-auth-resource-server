package org.entur.auth.spring.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Configuration of OAuth 2.0 Resource Server JWT
 * @see "https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html"
 */

@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ResourceServerAutoConfiguration {

    @EnableWebSecurity
    @EnableMethodSecurity(securedEnabled = true)
    @RequiredArgsConstructor
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public static class SecurityFilterChainBean {
        private final ConfigureMdcRequestFilter configureMdcRequestFilter;
        private final ConfigureAuthorizeRequests configureAuthorizeRequests;
        private final ConfigureAuth2ResourceServer configureAuth2ResourceServer;
        private final ConfigureCors configureCors;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws ResourceServerConfigurationException {
            try {
                return http
                        .sessionManagement(sessionManager-> sessionManager.sessionCreationPolicy(STATELESS))
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

    @ConditionalOnMissingBean(UserDetailsService.class)
    public static class UserDetailsServiceBean {
        @Bean
        public UserDetailsService userDetailsService() {
            return new NoUserDetailsService();  // avoid the default user.
        }
    }

    @ConditionalOnMissingBean(ConfigureMdcRequestFilter.class)
    public static class MdcRequestFilterBean {
        @Bean
        public ConfigureMdcRequestFilter configureMdcRequestFilter() {
            return new DefaultMdcRequestFilter();
        }
    }

    @ConditionalOnMissingBean(ConfigureAuthorizeRequests.class)
    public static class AuthorizeRequestsBean {
        @Bean
        public ConfigureAuthorizeRequests configureAuthorizeRequests() {
            return new DefaultConfigureAuthorizeRequests();
        }
    }

    @ConditionalOnMissingBean(ConfigureAuth2ResourceServer.class)
    public static class ConfigureAuth2ResourceServerBean {
        @Bean
        public ConfigureAuth2ResourceServer configureAuth2ResourceServer() {
            return new DefaultConfigureAuth2ResourceServer();
        }
    }

    @ConditionalOnMissingBean(ConfigureCors.class)
    public static class ConfigureConfigureCorsBean {
        @Bean
        public ConfigureCors configureCors() {
            return new DefaultConfigureCors();
        }
    }
}