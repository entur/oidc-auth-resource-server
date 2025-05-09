package org.entur.auth.spring.test.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.HttpServletRequest;
import org.entur.auth.spring.config.server.IssuerAuthenticationManagerResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/** Test files for use in config map */
@TestPropertySource(
        properties = {
            "entur.auth.tenants.environment=prd",
            "entur.auth.tenants.include=internal,traveller,partner,person",
            "entur.auth.test.load-environments=true",
            "entur.auth.lazy-load=true" // Test don't need to fetch JWKS
        })
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
class DefaultConfigurationPrdEnvironmentTest {

    @Autowired
    private AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

    @Test
    void testTenantHasBeenLoaded() {
        Assertions.assertInstanceOf(
                IssuerAuthenticationManagerResolver.class, authenticationManagerResolver);

        var resolver = (IssuerAuthenticationManagerResolver) authenticationManagerResolver;

        var issuers = resolver.getIssuers();

        assertEquals(4, issuers.size());

        assertTrue(issuers.contains("https://internal.entur.org/"));
        assertTrue(issuers.contains("https://partner.entur.org/"));
        assertTrue(issuers.contains("https://traveller.entur.org/"));
        assertTrue(issuers.contains("https://person.entur.org/"));
    }
}
