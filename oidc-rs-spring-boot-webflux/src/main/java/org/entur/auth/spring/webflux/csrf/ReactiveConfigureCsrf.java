package org.entur.auth.spring.webflux.csrf;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;

public interface ReactiveConfigureCsrf extends Customizer<ServerHttpSecurity.CsrfSpec> {}
