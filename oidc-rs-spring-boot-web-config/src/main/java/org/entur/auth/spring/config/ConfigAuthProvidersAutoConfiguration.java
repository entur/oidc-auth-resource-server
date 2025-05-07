package org.entur.auth.spring.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.common.server.AuthProviders;
import org.entur.auth.spring.common.server.DefaultAuthProviders;
import org.entur.auth.spring.common.server.ServerCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnMissingBean(AuthProviders.class)
@Conditional(ServerCondition.class)
@RequiredArgsConstructor
public class ConfigAuthProvidersAutoConfiguration {
    @Bean
    public AuthProviders authProviders() {
        log.debug("Configure DefaultAuthProviders");
        return new DefaultAuthProviders();
    }
}
