package org.entur.auth.junit.tenant;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.google.common.truth.Truth.assertThat;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TenantJsonWebToken.class)
class TenantJsonWebTokenTest {

    @Test
    void testPartnerTenant(
            @PartnerTenant(clientId = "abc", organisationId = 123L, username = "thomas")
                    String authorization,
            TenantAnnotationTokenFactory factory) {

        DecodedJWT decode = JWT.decode(authorization.substring(7));

        assertThat(decode.getClaim("azp").asString()).isEqualTo("abc");
        assertThat(decode.getClaim("preferred_username").asString()).isEqualTo("thomas");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);
    }

    @Test
    void testInternalTenant(
            @InternalTenant(clientId = "abc", organisationId = 123L) String authorization,
            TenantAnnotationTokenFactory factory) {

        DecodedJWT decode = JWT.decode(authorization.substring(7));

        assertThat(decode.getClaim("azp").asString()).isEqualTo("abc");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);
    }

    @Test
    void testTravellerTenant(
            @TravellerTenant(clientId = "abc", organisationId = 123L, customerNumber = "def")
                    String authorization,
            TenantAnnotationTokenFactory factory) {

        DecodedJWT decode = JWT.decode(authorization.substring(7));

        assertThat(decode.getClaim("azp").asString()).isEqualTo("abc");
        assertThat(decode.getClaim("https://entur.io/customerNumber").asString()).isEqualTo("def");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);
    }

    @Test
    void testPersonTenant(
            @PersonTenant(clientId = "abc", organisationId = 123L, socialSecurityNumber = "11223344556")
                    String authorization,
            TenantAnnotationTokenFactory factory) {

        DecodedJWT decode = JWT.decode(authorization.substring(7));

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

    @Test
    void testAddMyOwnMocking(
            @TravellerTenant(clientId = "abc", organisationId = 123L, customerNumber = "def")
                    String authorization,
            TenantAnnotationTokenFactory factory,
            WireMockServer mockServer)
            throws IOException, URISyntaxException {
        String body = "test response";
        mockServer.stubFor(
                get(urlEqualTo("/test"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/plain")
                                        .withBody(body)));

        DecodedJWT decode = JWT.decode(authorization.substring(7));

        assertThat(decode.getClaim("azp").asString()).isEqualTo("abc");
        assertThat(decode.getClaim("https://entur.io/customerNumber").asString()).isEqualTo("def");
        assertThat(decode.getClaim("https://entur.io/organisationID").asLong()).isEqualTo(123L);

        URI uri = new URI("http", null, "localhost", mockServer.port(), "/test", null, null);
        assertThat(IOUtils.toString(uri.toURL().openStream(), StandardCharsets.UTF_8)).isEqualTo(body);
    }
}
