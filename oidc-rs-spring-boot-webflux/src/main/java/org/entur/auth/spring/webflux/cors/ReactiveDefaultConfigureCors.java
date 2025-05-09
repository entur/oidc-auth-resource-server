package org.entur.auth.spring.webflux.cors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;

@Slf4j
public class ReactiveDefaultConfigureCors implements ReactiveConfigureCors {

    @Override
    public void customize(ServerHttpSecurity.CorsSpec corsSpec) {
        log.debug("Configure ReactiveDefaultConfigureCors");
        Customizer.withDefaults().customize(corsSpec);
    }
}
