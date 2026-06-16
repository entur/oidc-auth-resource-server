package org.entur.auth.spring.test.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.entur.auth.junit.tenant.WireMockAuthenticationServer;
import org.springframework.context.ApplicationContext;

/**
 * Shared assertions used by {@link MockServerPortInjectionTest} and {@link
 * MockServerPortInjectionReuseTest}. The two tests declare an identical Spring configuration so
 * they share a single cached application context. This verifies that the mock server port wired
 * into the context matches the live WireMock server, and that it stays consistent whenever the
 * context is reused.
 */
final class MockServerPortAssertions {

    // null until the first context has been seen; lets us detect and check context reuse.
    private static Integer firstContextId;
    private static Integer firstPort;

    private MockServerPortAssertions() {}

    /**
     * Assert that the port the Spring context was wired with equals the port the live WireMock server
     * actually listens on, and that this stays stable across reuses of the same cached context.
     */
    static synchronized void assertContextWiredToLiveServer(
            ApplicationContext context, WireMockAuthenticationServer server) {
        int livePort = server.getPort();

        // The system property is the channel the extension uses to communicate the port to Spring.
        assertEquals(
                Integer.toString(livePort),
                System.getProperty(TenantJsonWebToken.MOCKAUTHSERVER_PORT_NAME),
                "Published MOCKAUTHSERVER_PORT must match the running mock server port");

        int contextId = System.identityHashCode(context);
        if (firstContextId != null && firstContextId == contextId) {
            // Same cached context handed to another test class: the port must not have drifted.
            assertEquals(
                    firstPort,
                    livePort,
                    "A reused Spring context must keep the same mock server port it was built with");
        }

        firstContextId = contextId;
        firstPort = livePort;
    }
}
