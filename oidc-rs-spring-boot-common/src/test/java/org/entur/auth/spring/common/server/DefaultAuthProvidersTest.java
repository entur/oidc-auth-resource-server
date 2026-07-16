package org.entur.auth.spring.common.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Locks the channel used to communicate the mock server port to Spring: the {@code
 * MOCKAUTHSERVER_PORT} system property published by the JUnit extension is resolved into the mock
 * issuer certificate (JWKS) URLs that the resource server uses to validate tokens.
 */
class DefaultAuthProvidersTest {

    private static final String PORT_PROPERTY = "MOCKAUTHSERVER_PORT";
    private static final List<String> MOCK_TENANTS =
            List.of("internal", "traveller", "partner", "person");

    @Test
    void mockCertificateUrlsAreResolvedWithThePublishedPort() {
        String previous = System.getProperty(PORT_PROPERTY);
        try {
            System.setProperty(PORT_PROPERTY, "54321");

            List<IssuerProperties> providers = new DefaultAuthProviders().get("mock", MOCK_TENANTS);

            assertFalse(providers.isEmpty(), "Expected mock providers to be resolved");
            for (IssuerProperties provider : providers) {
                assertTrue(
                        provider.getCertificateUrl().contains("localhost:54321/"),
                        "Certificate URL must use the published port but was " + provider.getCertificateUrl());
                assertFalse(
                        provider.getCertificateUrl().contains("${MOCKAUTHSERVER_PORT}"),
                        "Placeholder must be resolved");
            }
        } finally {
            restore(previous);
        }
    }

    @Test
    void resolvedPortFollowsThePropertyWhenItChanges() {
        String previous = System.getProperty(PORT_PROPERTY);
        try {
            // Each resolution reads the property live, so a port retried onto a fresh value is picked
            // up rather than a stale one being cached.
            System.setProperty(PORT_PROPERTY, "11111");
            assertTrue(
                    firstCertificateUrl().contains("localhost:11111/"), "First resolution should use 11111");

            System.setProperty(PORT_PROPERTY, "22222");
            assertTrue(
                    firstCertificateUrl().contains("localhost:22222/"),
                    "Resolution after the port changes should use 22222");
        } finally {
            restore(previous);
        }
    }

    private static String firstCertificateUrl() {
        return new DefaultAuthProviders().get("mock", MOCK_TENANTS).get(0).getCertificateUrl();
    }

    private static void restore(String previous) {
        if (previous == null) {
            System.clearProperty(PORT_PROPERTY);
        } else {
            System.setProperty(PORT_PROPERTY, previous);
        }
    }
}
