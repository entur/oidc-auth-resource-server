package org.entur.auth.spring.webflux.mdc;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
public class ReactiveDefaultMdcRequestFilter implements ReactiveConfigureMdcRequestFilter {
    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
        log.debug("Configure ReactiveDefaultMdcRequestFilter");
        return chain.filter(exchange);
    }
}
