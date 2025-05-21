package org.entur.auth.junit.tenant;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.Getter;

/** A mock authentication server using WireMock to simulate authentication endpoints. */
public class WireMockAuthenticationServer implements AutoCloseable {

    /** The WireMock server instance. */
    // private final WireMockServer mockServer;
    private final WireMock wireMock;

    @Getter private final int port;

    /** Constructs a WireMock authentication server with a dynamically assigned port. */
    public WireMockAuthenticationServer() {
        var mockServer = new WireMockServer(wireMockConfig().dynamicPort());
        mockServer.start();

        wireMock = new WireMock(mockServer);
        port = mockServer.port();
    }

    /**
     * Constructs a WireMock authentication server with a specific port number.
     *
     * @param portNumber The port number for the WireMock server.
     */
    public WireMockAuthenticationServer(int portNumber) {
        var mockServer = new WireMockServer(wireMockConfig().port(portNumber));
        mockServer.start();

        wireMock = new WireMock(mockServer);
        port = mockServer.port();
    }

    public WireMockAuthenticationServer(WireMock wireMock, int port) {
        this.wireMock = wireMock;
        this.port = port;
    }

    /**
     * Sets up a json endpoint with a predefined response.
     *
     * @param certEndpoint The endpoint URL.
     * @param response The response body.
     */
    public void setJsonStubMappings(String certEndpoint, String response) {
        wireMock.register(
                get(urlEqualTo(certEndpoint))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/json")
                                        .withBody(response)));
    }

    /** Stops the mock server and releases resources. */
    public void close() {
        wireMock.shutdown();
    }

    /**
     * Returns the underlying WireMock server instance.
     *
     * @return The {@link WireMock} instance.
     */
    public WireMock getMockServer() {
        return wireMock;
    }
}
