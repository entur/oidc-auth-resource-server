package org.entur.auth.spring.webflux.csrf;

import org.springframework.security.config.web.server.ServerHttpSecurity;

public class ReactiveDefaultConfigureCsrf implements ReactiveConfigureCsrf {
    @Override
    public void customize(ServerHttpSecurity.CsrfSpec csrfSpec) {
        csrfSpec.disable();
    }
}
