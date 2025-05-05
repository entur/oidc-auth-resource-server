package org.entur.auth.spring.webflux.cors;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;

public interface ReactiveConfigureCors extends Customizer<ServerHttpSecurity.CorsSpec> {}
