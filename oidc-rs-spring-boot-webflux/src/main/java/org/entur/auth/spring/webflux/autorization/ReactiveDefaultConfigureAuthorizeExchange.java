package org.entur.auth.spring.webflux.autorization;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.web.server.ServerHttpSecurity;

@Slf4j
public class ReactiveDefaultConfigureAuthorizeExchange
        implements ReactiveConfigureAuthorizeExchange {

    @Override
    public void customize(ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchangeSpec) {
        log.info("Configure ReactiveDefaultConfigureAuthorizeExchange");
        authorizeExchangeSpec.anyExchange().authenticated();
    }
}
