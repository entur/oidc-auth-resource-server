package org.entur.auth.spring.test.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.entur.auth.junit.tenant.InternalTenant;
import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.entur.auth.junit.tenant.WireMockAuthenticationServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies that the Spring test context is wired with the same port as the WireMock mock server
 * bootstrapped by {@link TenantJsonWebToken}. {@link MockServerPortInjectionReuseTest} shares the
 * exact same Spring configuration so the two classes exercise a reused (cached) context.
 */
@ExtendWith({SpringExtension.class, TenantJsonWebToken.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MockServerPortInjectionTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ApplicationContext applicationContext;

    @Test
    void contextIsWiredWithLiveMockServerPort(
            @InternalTenant(clientId = "clientId") String authorization,
            WireMockAuthenticationServer mockServer)
            throws Exception {
        MockServerPortAssertions.assertContextWiredToLiveServer(applicationContext, mockServer);

        // End-to-end proof: the context resolved its JWKS URL to the live mock server port, so a
        // token signed by that server validates against the keys Spring fetches from it.
        var requestHeaders = new HttpHeaders();
        requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.add("Authorization", authorization);

        mockMvc.perform(get("/internal").headers(requestHeaders)).andExpect(status().isOk());
    }
}
