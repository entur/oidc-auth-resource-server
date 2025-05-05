package org.entur.auth.spring.webflux.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;

@Slf4j
public class ReactiveDefaultConfigureAuth2ResourceServer
        implements ReactiveConfigureAuth2ResourceServer {

    @Override
    public void customize(ServerHttpSecurity.OAuth2ResourceServerSpec oAuth2ResourceServerSpec) {
        log.info("Configure ReactiveDefaultConfigureAuth2ResourceServer");
        oAuth2ResourceServerSpec.jwt(Customizer.withDefaults());
    }
}
