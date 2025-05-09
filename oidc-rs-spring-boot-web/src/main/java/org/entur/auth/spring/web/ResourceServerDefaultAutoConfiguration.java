package org.entur.auth.spring.web;

import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.web.authorization.ConfigureAuthorizeRequests;
import org.entur.auth.spring.web.authorization.DefaultConfigureAuthorizeRequests;
import org.entur.auth.spring.web.cors.ConfigureCors;
import org.entur.auth.spring.web.cors.DefaultConfigureCors;
import org.entur.auth.spring.web.csrf.ConfigureCsrf;
import org.entur.auth.spring.web.csrf.DefaultConfigureCsrf;
import org.entur.auth.spring.web.mdc.ConfigureMdcRequestFilter;
import org.entur.auth.spring.web.mdc.DefaultMdcRequestFilter;
import org.entur.auth.spring.web.server.ConfigureAuth2ResourceServer;
import org.entur.auth.spring.web.server.DefaultConfigureAuth2ResourceServer;
import org.entur.auth.spring.web.session.ConfigureSessionManagement;
import org.entur.auth.spring.web.session.DefaultConfigureSessionManagement;
import org.entur.auth.spring.web.user.NoUserDetailsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ResourceServerDefaultAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService userDetailsService() {
        return new NoUserDetailsService(); // avoid the default user.
    }

    @Bean
    @ConditionalOnMissingBean(ConfigureAuthorizeRequests.class)
    public ConfigureAuthorizeRequests configureAuthorizeRequests() {
        return new DefaultConfigureAuthorizeRequests();
    }

    @Bean
    @ConditionalOnMissingBean(ConfigureSessionManagement.class)
    public ConfigureSessionManagement configureSessionManagement() {
        return new DefaultConfigureSessionManagement();
    }

    @Bean
    @ConditionalOnMissingBean(ConfigureCsrf.class)
    public ConfigureCsrf configureCsrf() {
        return new DefaultConfigureCsrf();
    }

    @Bean
    @ConditionalOnMissingBean(ConfigureCors.class)
    public ConfigureCors configureCors() {
        return new DefaultConfigureCors();
    }

    @Bean
    @ConditionalOnMissingBean(ConfigureMdcRequestFilter.class)
    public ConfigureMdcRequestFilter configureMdcRequestFilter() {
        return new DefaultMdcRequestFilter();
    }

    @Bean
    @ConditionalOnMissingBean(ConfigureAuth2ResourceServer.class)
    public ConfigureAuth2ResourceServer configureAuth2ResourceServer() {
        return new DefaultConfigureAuth2ResourceServer();
    }
}
