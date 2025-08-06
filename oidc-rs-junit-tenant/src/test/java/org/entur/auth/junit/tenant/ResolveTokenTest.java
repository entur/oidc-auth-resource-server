package org.entur.auth.junit.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@ExtendWith(TenantJsonWebToken.class)
@Execution(ExecutionMode.CONCURRENT)
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

        assertEquals("testSubject", decode.getClaim("sub").asString());
        assertIterableEquals(List.of("testAudience"), decode.getClaim("aud").asList(String.class));
        assertEquals("write", decode.getClaim("scope").asString());
        assertIterableEquals(List.of("a", "b", "c"), decode.getClaim("roles").asList(String.class));
        assertEquals(123L, decode.getClaim("https://entur.io/organisationID").asLong());
        assertIterableEquals(List.of(1L, 2L, 3L), decode.getClaim("ids").asList(Long.class));
        assertEquals(true, decode.getClaim("email_verified").asBoolean());
    }

    @Test
    void testPartnerTenant(
            @PartnerTenant(
                            clientId = "abc",
                            username = "testUser",
                            permissions = {"person:read"})
                    String token) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertEquals("abc", decode.getClaim("azp").asString());
        assertEquals("testUser", decode.getClaim("preferred_username").asString());
        assertIterableEquals(
                List.of("person:read"), decode.getClaim("permissions").asList(String.class));
        assertEquals(123L, decode.getClaim("https://entur.io/organisationID").asLong());
    }

    @Test
    void testInternalTenant(@InternalTenant(clientId = "abc", organisationId = 1234L) String token) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertEquals("abc", decode.getClaim("azp").asString());
        assertEquals(1234L, decode.getClaim("https://entur.io/organisationID").asLong());
    }

    @Test
    void testTravellerTenant(
            @TravellerTenant(clientId = "abc", organisationId = 1234L, customerNumber = "def")
                    String token) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertEquals("abc", decode.getClaim("azp").asString());
        assertEquals("def", decode.getClaim("https://entur.io/customerNumber").asString());
        assertEquals(1234L, decode.getClaim("https://entur.io/organisationID").asLong());
    }

    @Test
    void testPersonTenant(
            @PersonTenant(clientId = "abc", organisationId = 1234L, socialSecurityNumber = "11223344556")
                    String token) {

        DecodedJWT decode = JWT.decode(token.substring(7));

        assertEquals("abc", decode.getClaim("azp").asString());
        assertEquals("11223344556", decode.getClaim("https://entur.io/ssn").asString());
        assertEquals(1234L, decode.getClaim("https://entur.io/organisationID").asLong());
    }

    /** Test that creating an expired token actually works */
    @Test
    void testExpiredToken(@PartnerTenant(expiresInMinutes = -1) String authorization) {

        DecodedJWT decode = JWT.decode(authorization.substring(7));
        assertTrue(decode.getExpiresAtAsInstant().isBefore(Instant.now()), "expiresAt is after now");
    }
}
