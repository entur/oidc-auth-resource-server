package org.entur.auth.spring.common.server;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSetBasedJWKSource;
import com.nimbusds.jose.jwk.source.JWKSetSource;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JWKSourceWithIssuer<C extends SecurityContext> implements JWKSource<C> {
    @Getter private final String issuerUrl;

    private final JWKSource<SecurityContext> jwkSource;

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext securityContext)
            throws KeySourceException {
        return jwkSource.get(jwkSelector, securityContext);
    }

    public boolean getReadiness() {
        return true;
    }

    public @NonNull JWKSetSource<SecurityContext> getJWKSetSource() {
        if (jwkSource instanceof JWKSetBasedJWKSource<SecurityContext> source)
            return source.getJWKSetSource();

        throw new IllegalStateException(
                "Not an instance of %s".formatted(JWKSetBasedJWKSource.class.getSimpleName()));
    }
}
