package org.entur.auth.spring.test.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import jakarta.servlet.http.HttpServletRequest;
import org.entur.auth.junit.tenant.PartnerTenant;
import org.entur.auth.junit.tenant.TenantAnnotationTokenFactory;
import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.entur.auth.spring.common.server.IssuerProperties;
import org.entur.auth.spring.config.server.IssuerAuthenticationManagerResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("annotation")
@ExtendWith({SpringExtension.class, TenantJsonWebToken.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WireMockTest
class WireMockTestAnnotationTest {
    @Autowired private MockMvc mockMvc;

    @Autowired
    private AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

    @BeforeEach
    void beforeEachTests(
            TenantAnnotationTokenFactory annotationTokenFactory, WireMockRuntimeInfo wmRuntimeInfo) {

        annotationTokenFactory.setServer(wmRuntimeInfo.getWireMock(), wmRuntimeInfo.getHttpPort());
        ((IssuerAuthenticationManagerResolver) authenticationManagerResolver)
                .addIssuer(
                        IssuerProperties.builder()
                                .issuerUrl("https://partner.mock.entur.io")
                                .certificateUrl(
                                        "http://localhost:"
                                                + wmRuntimeInfo.getHttpPort()
                                                + "/partner/.well-known/jwks.json")
                                .build());
    }

    @Test
    void testProtectedWithPartner(
            @PartnerTenant(clientId = "clientId", subject = "subject") String authorization)
            throws Exception {
        var requestHeaders = new HttpHeaders();
        requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.add("Authorization", authorization);

        mockMvc.perform(get("/protected").headers(requestHeaders)).andExpect(status().isOk());
    }

    @AfterAll
    static void aferAllTests(TenantAnnotationTokenFactory annotationTokenFactory) {
        annotationTokenFactory.shutdown();
    }
}
