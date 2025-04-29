package org.entur.auth.spring.config.server;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;

public final class DisposableAuthenticationManagerResolver
        implements AuthenticationManagerResolver<HttpServletRequest>, SupportsReadiness {
    private final AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;
    private final List<JWKSourceWithIssuer> remoteJWKSets;

    public DisposableAuthenticationManagerResolver(
            @NonNull AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver,
            @NonNull List<JWKSourceWithIssuer> remoteJWKSets) {
        this.authenticationManagerResolver = authenticationManagerResolver;
        this.remoteJWKSets = new ArrayList<>(remoteJWKSets);
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest context) {
        return authenticationManagerResolver.resolve(context);
    }

    public Set<String> getIssuers() {
        return remoteJWKSets.stream()
                .map(JWKSourceWithIssuer::getIssuerUrl)
                .collect(Collectors.toSet());
    }

    public boolean getReadiness() {
        return true; // remoteJWKSets.stream().allMatch(OAuth2RemoteJWKSet::getReadiness);
    }
}
