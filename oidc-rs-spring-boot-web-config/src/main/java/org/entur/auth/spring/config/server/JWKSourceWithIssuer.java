package org.entur.auth.spring.config.server;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JWKSourceWithIssuer implements JWKSource<SecurityContext> {
    @Getter private final String issuerUrl;

    private final JWKSource<SecurityContext> jwkSource;

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext securityContext)
            throws KeySourceException {
        return jwkSource.get(jwkSelector, securityContext);
    }
}
