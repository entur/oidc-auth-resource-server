package org.entur.auth.spring.config.server;

import com.nimbusds.jose.jwk.source.JWKSetSourceWithHealthStatusReporting;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.health.HealthReportListener;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;

public final class IssuerAuthenticationManagerResolver
        implements AuthenticationManagerResolver<HttpServletRequest>, SupportsReadiness {

    private final Map<String, AuthenticationManager> authenticationManagers;
    private final List<JWKSourceWithIssuer> remoteJWKSets;
    private final EnturAuthProperties enturAuthProperties;
    private final TenantJwtGrantedAuthoritiesConverter authoritiesConverter;
    private final HealthReportListener<
                    JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext>
            healthReportListener;
    private final AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

    public IssuerAuthenticationManagerResolver(
            @NonNull Map<String, AuthenticationManager> authenticationManagers,
            @NonNull List<JWKSourceWithIssuer> remoteJWKSets,
            @NonNull EnturAuthProperties enturAuthProperties,
            @NonNull TenantJwtGrantedAuthoritiesConverter authoritiesConverter,
            @NonNull
                    HealthReportListener<
                                    JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext>
                            healthReportListener) {

        this.authenticationManagers = authenticationManagers;
        this.remoteJWKSets = new ArrayList<>(remoteJWKSets);
        this.enturAuthProperties = enturAuthProperties;
        this.authoritiesConverter = authoritiesConverter;
        this.healthReportListener = healthReportListener;

        this.authenticationManagerResolver =
                new JwtIssuerAuthenticationManagerResolver(authenticationManagers::get);
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
        return remoteJWKSets.stream().allMatch(JWKSourceWithIssuer::getReadiness);
    }

    public void addIssuer(@NonNull IssuerProperties issuerProperties) {
        if (remoteJWKSets.stream()
                .anyMatch(
                        jwkSourceWithIssuer ->
                                Objects.equals(
                                        jwkSourceWithIssuer.getIssuerUrl(), issuerProperties.getIssuerUrl()))) {
            return;
        }

        AuthenticationManagerFactory.add(
                authenticationManagers,
                remoteJWKSets,
                issuerProperties,
                enturAuthProperties,
                authoritiesConverter,
                healthReportListener);
    }
}
