package org.entur.auth.spring.web.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

@Slf4j
public class DefaultConfigureAuth2ResourceServer implements ConfigureAuth2ResourceServer {
    @Override
    public void customize(
            OAuth2ResourceServerConfigurer<HttpSecurity> httpSecurityOAuth2ResourceServerConfigurer) {
        log.debug("Configure DefaultConfigureAuth2ResourceServer");
        httpSecurityOAuth2ResourceServerConfigurer.jwt(Customizer.withDefaults());
    }
}
