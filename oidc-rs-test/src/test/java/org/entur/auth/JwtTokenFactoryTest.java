package org.entur.auth;
import static com.google.common.truth.Truth.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtTokenFactoryTest {
    private static final String DOMAIN_TENANT = "default";
    private static final String CLAIM_ORGANISATION_ID = "https://entur.io/organisationID";
    private static final Provider provider = new Provider() {};
    private final JwtTokenFactory factory = new JwtTokenFactory(provider, DOMAIN_TENANT);

    @Test
    void testAudienceWorks() {
        String token = factory.jwtTokenBuilder()
                .provider(provider)
                .domain(DOMAIN_TENANT)
                .audience("https://api.dev.entur.io")
                .expiresInMinutes(5)
                .claims(Map.of(
                        CLAIM_ORGANISATION_ID, 12345678L,
                        Provider.CLAIM_AZP, "ABC",
                        Provider.CLAIM_PREFERRED_USERNAME, "myUsername"

                ))
                .create();

        DecodedJWT decode = JWT.decode(token);
        
        // check other claims
        assertThat(decode.getClaim(CLAIM_ORGANISATION_ID).asLong()).isEqualTo(12345678L);
        assertThat(decode.getClaim(Provider.CLAIM_AZP).asString()).isEqualTo("ABC");
        assertThat(decode.getClaim(Provider.CLAIM_PREFERRED_USERNAME).asString()).isEqualTo("myUsername");
        assertThat(decode.getAudience().getFirst()).isEqualTo("https://api.dev.entur.io");
        assertThat(decode.getIssuer()).contains(DOMAIN_TENANT);
    }
}
