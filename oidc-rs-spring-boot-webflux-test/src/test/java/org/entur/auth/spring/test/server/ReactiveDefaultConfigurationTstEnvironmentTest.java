package org.entur.auth.spring.test.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.entur.auth.spring.config.server.ReactiveIssuerAuthenticationManagerResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ServerWebExchange;

/** Test files for use in config map */
@TestPropertySource(
        properties = {
            "entur.auth.tenants.environment=tst",
            "entur.auth.tenants.include=internal,traveller,partner,person",
            "entur.auth.test.load-environments=true",
            "entur.auth.lazy-load=true" // Test don't need to fetch JWKS
        })
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext
class ReactiveDefaultConfigurationTstEnvironmentTest {

    @Autowired
    private ReactiveAuthenticationManagerResolver<ServerWebExchange> authenticationManagerResolver;

    @Test
    void testTenantHasBeenLoaded() {
        assertInstanceOf(
                ReactiveIssuerAuthenticationManagerResolver.class, authenticationManagerResolver);

        var resolver = (ReactiveIssuerAuthenticationManagerResolver) authenticationManagerResolver;

        var issuers = resolver.getIssuers();

        assertEquals(4, issuers.size());

        assertTrue(issuers.contains("https://internal.staging.entur.org/"));
        assertTrue(issuers.contains("https://partner.staging.entur.org/"));
        assertTrue(issuers.contains("https://traveller.staging.entur.org/"));
        assertTrue(issuers.contains("https://person.staging.entur.org/"));
    }
}
