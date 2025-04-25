package org.entur.auth.spring.config.server;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.entur.auth.spring.jwk.OAuth2RemoteJWKSet;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class DisposableAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest>, DisposableBean, SupportsReadiness {
    private final AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;
    private final List<OAuth2RemoteJWKSet<?>> remoteJWKSets;

    public DisposableAuthenticationManagerResolver(@NonNull AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver, @NonNull List<OAuth2RemoteJWKSet<?>> remoteJWKSets) {
        this.authenticationManagerResolver = authenticationManagerResolver;
        this.remoteJWKSets = new ArrayList<>(remoteJWKSets);
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest context) {
        return authenticationManagerResolver.resolve(context);
    }

    @Override
    public void destroy()  {
        remoteJWKSets.forEach(OAuth2RemoteJWKSet::shutdown);
    }

    public Set<String> getIssuers() {
        return remoteJWKSets.stream().map(OAuth2RemoteJWKSet::getIssuerUri).collect(Collectors.toSet());
    }

    public boolean getReadiness() {
        return remoteJWKSets.stream().allMatch(OAuth2RemoteJWKSet::getReadiness);
    }
}