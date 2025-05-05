package org.entur.auth.spring.webflux.autorization;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;

public interface ReactiveConfigureAuthorizeExchange
        extends Customizer<ServerHttpSecurity.AuthorizeExchangeSpec> {}
