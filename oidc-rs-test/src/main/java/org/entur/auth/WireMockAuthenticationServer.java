package org.entur.auth;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;

/**
 * A mock authentication server using WireMock to simulate authentication endpoints.
 */
public class WireMockAuthenticationServer implements AutoCloseable {

    /**
     * The WireMock server instance.
     */
    private final WireMockServer mockServer;

    /**
     * Constructs a WireMock authentication server with a dynamically assigned port.
     */
    public WireMockAuthenticationServer() {
        mockServer = new WireMockServer(wireMockConfig().dynamicPort());
    }

    /**
     * Constructs a WireMock authentication server with a specific port number.
     *
     * @param portNumber The port number for the WireMock server.
     */
    public WireMockAuthenticationServer(int portNumber) {
        mockServer = new WireMockServer(wireMockConfig().port(portNumber));
    }

    /**
     * Sets up a json endpoint with a predefined response.
     *
     * @param certEndpoint The endpoint URL.
     * @param response     The response body.
     */
    public void setJsonStubMappings(String certEndpoint, String response) {
        mockServer.stubFor(get(urlEqualTo(certEndpoint))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/json")
                        .withBody(response)));
    }

    /**
     * Returns the port on which the mock server is running.
     *
     * @return The port number.
     */
    public int getPort() {
        return mockServer.port();
    }

    /**
     * Starts the mock server and sets the "MOCKAUTHSERVER_PORT" system property.
     */
    public void start() {
        mockServer.start();
    }

    /**
     * Checks if the mock server is currently running.
     *
     * @return {@code true} if the server is running, {@code false} otherwise.
     */
    public boolean isRunning() {
        return mockServer.isRunning();
    }

    /**
     * Stops the mock server and releases resources.
     */
    public void close() {
	    mockServer.stop();
        mockServer.shutdown();
    }

    /**
     * Resets all WireMock mappings and requests.
     */
    public void reset() {
        mockServer.resetAll();
    }

    /**
     * Returns the underlying WireMock server instance.
     *
     * @return The {@link WireMockServer} instance.
     */
    public WireMockServer getMockServer() {
        return mockServer;
    }
}
