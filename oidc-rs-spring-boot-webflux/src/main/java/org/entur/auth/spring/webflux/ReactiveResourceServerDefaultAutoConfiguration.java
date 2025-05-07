package org.entur.auth.spring.webflux;

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
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;

@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveResourceServerDefaultAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ReactiveUserDetailsService.class)
    public ReactiveUserDetailsService userDetailsService() {
        return new ReactiveNoUserDetailsService();
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveConfigureAuthorizeExchange.class)
    public ReactiveConfigureAuthorizeExchange reactiveConfigureAuthorizeExchange() {
        return new ReactiveDefaultConfigureAuthorizeExchange();
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveConfigureCors.class)
    public ReactiveConfigureCors reactiveConfigureCors() {
        return new ReactiveDefaultConfigureCors();
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveConfigureMdcRequestFilter.class)
    public ReactiveConfigureMdcRequestFilter reactiveConfigureMdcRequestFilter() {
        return new ReactiveDefaultMdcRequestFilter();
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveConfigureAuth2ResourceServer.class)
    public ReactiveConfigureAuth2ResourceServer reactiveConfigureAuth2ResourceServer() {
        return new ReactiveDefaultConfigureAuth2ResourceServer();
    }
}
