package org.entur.auth.junit.tenant;

import static com.google.common.truth.Truth.assertThat;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TenantJsonWebToken.class)
class ResolveTokenTest {

    @Test
    void testPartnerTenant(
            @PartnerTenant(clientId = "abc", username = "testUser") String token,
            TenantAnnotationTokenFactory factory) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertThat(decode.getClaim("azp").asString()).isEqualTo("abc");
        assertThat(decode.getClaim("preferred_username").asString()).isEqualTo("testUser");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);
    }

    @Test
    void testInternalTenant(
            @InternalTenant(clientId = "abc", organisationId = 123L) String token,
            TenantAnnotationTokenFactory factory) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertThat(decode.getClaim("azp").asString()).isEqualTo("abc");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);
    }

    @Test
    void testTravellerTenant(
            @TravellerTenant(clientId = "abc", organisationId = 123L, customerNumber = "def")
                    String token,
            TenantAnnotationTokenFactory factory) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertThat(decode.getClaim("azp").asString()).isEqualTo("abc");
        assertThat(decode.getClaim("https://entur.io/customerNumber").asString()).isEqualTo("def");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);
    }

    @Test
    void testPersonTenant(
            @PersonTenant(clientId = "abc", organisationId = 123L, socialSecurityNumber = "11223344556")
                    String token,
            TenantAnnotationTokenFactory factory) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertThat(decode.getClaim("azp").asString()).isEqualTo("abc");
        assertThat(decode.getClaim("https://entur.io/ssn").asString()).isEqualTo("11223344556");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);
    }

    /** Test that creating an expired token actually works */
    @Test
    void testExpiredToken(
            @PartnerTenant(expiresInMinutes = -1) String authorization,
            TenantAnnotationTokenFactory factory) {

        DecodedJWT decode = JWT.decode(authorization.substring(7));
        assertThat(decode.getExpiresAtAsInstant()).isAtMost(Instant.now());
    }
}
