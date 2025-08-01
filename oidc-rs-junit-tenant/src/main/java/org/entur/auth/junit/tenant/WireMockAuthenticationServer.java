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

    /**
     * A lightweight mock authentication server leveraging WireMock to simulate HTTP-based
     * authentication endpoints.
     *
     * <p><strong>Usage:</strong> Instantiate this server on a dynamic or fixed port, register stub
     * mappings, and integrate it into integration tests to emulate external authentication services.
     *
     * <p>The server implements {@link AutoCloseable} so it can be used in try-with-resources blocks
     * to ensure clean shutdown and resource release.
     *
     * @since 1.0
     */
    private final WireMock wireMock;

    /** TCP port on which the mock server is listening. */
    @Getter private final int port;

    /**
     * Creates a new mock authentication server on a dynamically assigned free port.
     *
     * <p>The server is immediately started and ready to accept stub mappings.
     */
    public WireMockAuthenticationServer() {
        var mockServer = new WireMockServer(wireMockConfig().dynamicPort());
        mockServer.start();

        wireMock = new WireMock(mockServer);
        port = mockServer.port();
    }

    /**
     * Creates a new mock authentication server bound to the specified port.
     *
     * <p>Use this constructor when tests need to target a known, fixed port.
     *
     * @param portNumber the TCP port number for the mock server; must be available on the local
     *     machine
     */
    public WireMockAuthenticationServer(int portNumber) {
        var mockServer = new WireMockServer(wireMockConfig().port(portNumber));
        mockServer.start();

        wireMock = new WireMock(mockServer);
        port = mockServer.port();
    }

    /**
     * Wraps an existing WireMock client and port without starting a new server.
     *
     * <p>Primarily used for advanced scenarios where the WireMock server lifecycle is managed
     * externally or shared across multiple test fixtures.
     *
     * @param wireMock the WireMock client instance connected to a running server
     * @param port the port on which the external WireMock server is running
     */
    public WireMockAuthenticationServer(WireMock wireMock, int port) {
        this.wireMock = wireMock;
        this.port = port;
    }

    /**
     * Registers a stub mapping for a JSON endpoint with a predefined response payload.
     *
     * <p>The stub will return HTTP 200 with the given response body and Content-Type header.
     *
     * @param endpointPath the request path (e.g. "/.well-known/jwks.json") to stub
     * @param jsonResponse the JSON response body to return when the path is requested
     */
    public void setJsonStubMappings(String endpointPath, String jsonResponse) {
        wireMock.register(
                get(urlEqualTo(endpointPath))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/json")
                                        .withBody(jsonResponse)));
    }

    /**
     * Stops the mock server and releases all associated resources.
     *
     * <p>If the server was not started by this instance, this call will still request shutdown, but
     * behavior depends on the external server configuration.
     */
    @Override
    public void close() {
        wireMock.shutdown();
    }

    /**
     * Provides direct access to the underlying WireMock client for advanced stub configuration.
     *
     * @return the {@link WireMock} client instance
     */
    public WireMock getMockServer() {
        return wireMock;
    }
}
