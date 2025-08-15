package org.entur.auth.spring.config.server;

import com.nimbusds.jose.jwk.source.JWKSetSourceWithHealthStatusReporting;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.health.HealthReportListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.entur.auth.spring.common.server.EnturAuthProperties;
import org.entur.auth.spring.common.server.IssuerProperties;
import org.entur.auth.spring.common.server.SupportsReadiness;
import org.entur.auth.spring.common.server.TenantJwtGrantedAuthoritiesConverter;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public final class ReactiveIssuerAuthenticationManagerResolver
        implements ReactiveAuthenticationManagerResolver<ServerWebExchange>, SupportsReadiness {

    private final Map<String, ReactiveAuthenticationManager> authenticationManagers;
    private final List<ReactiveJWKSourceWithIssuer> remoteJWKSets;
    private final EnturAuthProperties enturAuthProperties;
    private final TenantJwtGrantedAuthoritiesConverter authoritiesConverter;
    private final HealthReportListener<
                    JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext>
            healthReportListener;
    private final ReactiveAuthenticationManagerResolver<ServerWebExchange>
            authenticationManagerResolver;

    public ReactiveIssuerAuthenticationManagerResolver(
            @NonNull Map<String, ReactiveAuthenticationManager> authenticationManagers,
            @NonNull List<ReactiveJWKSourceWithIssuer> remoteJWKSets,
            @NonNull EnturAuthProperties enturAuthProperties,
            @NonNull TenantJwtGrantedAuthoritiesConverter authoritiesConverter,
            HealthReportListener<JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext>
                    healthReportListener) {

        this.authenticationManagers = authenticationManagers;
        this.remoteJWKSets = new ArrayList<>(remoteJWKSets);
        this.enturAuthProperties = enturAuthProperties;
        this.authoritiesConverter = authoritiesConverter;
        this.healthReportListener = healthReportListener;

        this.authenticationManagerResolver =
                new JwtIssuerReactiveAuthenticationManagerResolver(
                        issuer -> Mono.justOrEmpty(authenticationManagers.get(issuer)));
    }

    @Override
    public Mono<ReactiveAuthenticationManager> resolve(ServerWebExchange exchange) {
        return authenticationManagerResolver.resolve(exchange);
    }

    public Set<String> getIssuers() {
        return remoteJWKSets.stream()
                .map(ReactiveJWKSourceWithIssuer::getIssuerUrl)
                .collect(Collectors.toSet());
    }

    public boolean getReadiness() {
        return remoteJWKSets.stream().allMatch(ReactiveJWKSourceWithIssuer::getReadiness);
    }

    public void addIssuer(@NonNull IssuerProperties issuerProperties) {
        if (remoteJWKSets.stream()
                .anyMatch(
                        jwkSourceWithIssuer ->
                                Objects.equals(
                                        jwkSourceWithIssuer.getIssuerUrl(), issuerProperties.getIssuerUrl()))) {
            return;
        }

        ReactiveAuthenticationManagerFactory.add(
                authenticationManagers,
                remoteJWKSets,
                issuerProperties,
                enturAuthProperties,
                authoritiesConverter,
                healthReportListener);
    }
}
