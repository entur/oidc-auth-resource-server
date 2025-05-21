package org.entur.auth.junit.tenant;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TenantJsonWebToken.class)
class ResolveServerTest {
    @Test
    void testAddMyOwnMocking(
            @TravellerTenant(clientId = "abc", organisationId = 1234L, customerNumber = "def")
                    String authorization,
            WireMockAuthenticationServer wireMockAuthenticationServer,
            WireMock wireMock)
            throws IOException, URISyntaxException {
        String body = "test response";
        wireMock.register(
                get(urlEqualTo("/test"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/plain")
                                        .withBody(body)));

        DecodedJWT decode = JWT.decode(authorization.substring(7));

        assertEquals("abc", decode.getClaim("azp").asString());
        assertEquals("def", decode.getClaim("https://entur.io/customerNumber").asString());
        assertEquals(1234L, decode.getClaim("https://entur.io/organisationID").asLong());

        URI uri =
                new URI(
                        "http", null, "localhost", wireMockAuthenticationServer.getPort(), "/test", null, null);
        assertEquals(body, IOUtils.toString(uri.toURL().openStream(), StandardCharsets.UTF_8));
    }
}
