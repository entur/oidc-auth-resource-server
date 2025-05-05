package org.entur.auth.spring.config.mdc;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.common.mdc.MdcFromToProperties;
import org.entur.auth.spring.common.mdc.MdcProperties;
import org.entur.auth.spring.webflux.mdc.ReactiveConfigureMdcRequestFilter;
import org.slf4j.MDC;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
public class ReactiveMdcRequestFilter implements ReactiveConfigureMdcRequestFilter {
    private final List<MdcFromToProperties> mappings = new ArrayList<>();

    public ReactiveMdcRequestFilter(MdcProperties mdcProperties) {
        this.mappings.addAll(mdcProperties.getMappings());
        if (this.mappings.isEmpty()) {
            this.mappings.add(new MdcFromToProperties("azp", "clientId"));
            this.mappings.add(
                    new MdcFromToProperties("https://entur.io/organisationID", "organisationId"));
        }
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        return exchange
                .getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .map(AbstractOAuth2TokenAuthenticationToken::getToken)
                .doOnNext(principal -> addMDC(mappings, principal))
                .zipWhen(tenant -> chain.filter(exchange))
                .doOnNext(tuple2 -> removeMDC(mappings))
                .map(Tuple2::getT2);
    }

    private static void addMDC(@NonNull List<MdcFromToProperties> mappings, Jwt token) {
        if (token == null) {
            return;
        }

        mappings.forEach(
                mdcFromToProperties -> {
                    Object value = token.getClaim(mdcFromToProperties.getFrom());
                    if (value != null) {
                        MDC.put(mdcFromToProperties.getTo(), value.toString());
                    }
                });
    }

    private static void removeMDC(@NonNull List<MdcFromToProperties> mappings) {
        mappings.forEach(mdcFromToProperties -> MDC.remove(mdcFromToProperties.getTo()));
    }
}
