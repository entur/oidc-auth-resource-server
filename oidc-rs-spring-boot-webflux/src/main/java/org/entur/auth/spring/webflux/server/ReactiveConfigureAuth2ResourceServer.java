package org.entur.auth.spring.webflux.server;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;

public interface ReactiveConfigureAuth2ResourceServer
        extends Customizer<ServerHttpSecurity.OAuth2ResourceServerSpec> {}
