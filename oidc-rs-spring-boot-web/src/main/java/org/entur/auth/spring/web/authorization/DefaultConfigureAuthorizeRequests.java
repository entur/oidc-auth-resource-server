package org.entur.auth.spring.web.authorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@Slf4j
public class DefaultConfigureAuthorizeRequests implements ConfigureAuthorizeRequests {
    @Override
    public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorizationManagerRequestMatcherRegistry) {
        log.info("Configure DefaultConfigureAuthorizeRequests");
        authorizationManagerRequestMatcherRegistry.anyRequest().authenticated();

    }
}
