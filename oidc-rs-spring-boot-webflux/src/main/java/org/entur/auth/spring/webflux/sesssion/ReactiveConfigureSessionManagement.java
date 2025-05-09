package org.entur.auth.spring.webflux.sesssion;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;

public interface ReactiveConfigureSessionManagement
        extends Customizer<ServerHttpSecurity.RequestCacheSpec> {}
