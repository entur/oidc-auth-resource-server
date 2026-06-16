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
 * Second class with the same Spring configuration as {@link MockServerPortInjectionTest}. Spring
 * caches and reuses the application context across both classes; this asserts the reused context
 * keeps the same mock server port and still authenticates against the live server.
 */
@ExtendWith({SpringExtension.class, TenantJsonWebToken.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MockServerPortInjectionReuseTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ApplicationContext applicationContext;

    @Test
    void reusedContextKeepsLiveMockServerPort(
            @InternalTenant(clientId = "clientId") String authorization,
            WireMockAuthenticationServer mockServer)
            throws Exception {
        MockServerPortAssertions.assertContextWiredToLiveServer(applicationContext, mockServer);

        // End-to-end proof: the reused context still resolves its JWKS URL to the live mock server
        // port, so a token signed by that server validates against the keys Spring fetches from it.
        var requestHeaders = new HttpHeaders();
        requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.add("Authorization", authorization);

        mockMvc.perform(get("/internal").headers(requestHeaders)).andExpect(status().isOk());
    }
}
