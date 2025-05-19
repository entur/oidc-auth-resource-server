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
    void testTenantToken(
            @TenantToken(
                            subject = "testSubject",
                            audience = "testAudience",
                            stringClaims = {@StringClaim(name = "scope", value = "write")},
                            stringArrayClaims = {
                                @StringArrayClaim(
                                        name = "roles",
                                        value = {"a", "b", "c"})
                            },
                            longClaims = {@LongClaim(name = "https://entur.io/organisationID", value = 123L)},
                            longArrayClaims = {
                                @LongArrayClaim(
                                        name = "ids",
                                        value = {1L, 2L, 3L})
                            },
                            booleanClaims = {@BooleanClaim(name = "email_verified", value = true)})
                    String token) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertThat(decode.getClaim("sub").asString()).isEqualTo("testSubject");
        assertThat(decode.getClaim("aud").asList(String.class)).containsExactly("testAudience");
        assertThat(decode.getClaim("scope").asString()).isEqualTo("write");
        assertThat(decode.getClaim("roles").asList(String.class)).containsExactly("a", "b", "c");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);
        assertThat(decode.getClaim("ids").asList(Long.class)).containsExactly(1L, 2L, 3L);
        assertThat(decode.getClaim("email_verified").asBoolean()).isEqualTo(true);
    }

    @Test
    void testPartnerTenant(
            @PartnerTenant(
                            clientId = "abc",
                            username = "testUser",
                            permissions = {"person:read"})
                    String token) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertThat(decode.getClaim("azp").asString()).isEqualTo("abc");
        assertThat(decode.getClaim("preferred_username").asString()).isEqualTo("testUser");
        assertThat(decode.getClaim("permissions").asList(String.class)).containsExactly("person:read");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);
    }

    @Test
    void testInternalTenant(@InternalTenant(clientId = "abc", organisationId = 123L) String token) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertThat(decode.getClaim("azp").asString()).isEqualTo("abc");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);
    }

    @Test
    void testTravellerTenant(
            @TravellerTenant(clientId = "abc", organisationId = 123L, customerNumber = "def")
                    String token) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertThat(decode.getClaim("azp").asString()).isEqualTo("abc");
        assertThat(decode.getClaim("https://entur.io/customerNumber").asString()).isEqualTo("def");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);
    }

    @Test
    void testPersonTenant(
            @PersonTenant(clientId = "abc", organisationId = 123L, socialSecurityNumber = "11223344556")
                    String token) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertThat(decode.getClaim("azp").asString()).isEqualTo("abc");
        assertThat(decode.getClaim("https://entur.io/ssn").asString()).isEqualTo("11223344556");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);
    }

    /** Test that creating an expired token actually works */
    @Test
    void testExpiredToken(@PartnerTenant(expiresInMinutes = -1) String authorization) {

        DecodedJWT decode = JWT.decode(authorization.substring(7));
        assertThat(decode.getExpiresAtAsInstant()).isAtMost(Instant.now());
    }
}
