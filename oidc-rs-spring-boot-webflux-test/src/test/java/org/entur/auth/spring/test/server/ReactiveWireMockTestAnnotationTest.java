package org.entur.auth.spring.test.server;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.entur.auth.junit.tenant.PartnerTenant;
import org.entur.auth.junit.tenant.TenantAnnotationTokenFactory;
import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.entur.auth.spring.common.server.IssuerProperties;
import org.entur.auth.spring.config.server.ReactiveIssuerAuthenticationManagerResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;

@ActiveProfiles("annotation")
@ExtendWith({SpringExtension.class, TenantJsonWebToken.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@WireMockTest
class ReactiveWireMockTestAnnotationTest {
    @Autowired private WebTestClient webTestClient;

    @Autowired
    private ReactiveAuthenticationManagerResolver<ServerWebExchange> authenticationManagerResolver;

    @BeforeEach
    void beforeEachTests(
            TenantAnnotationTokenFactory annotationTokenFactory, WireMockRuntimeInfo wmRuntimeInfo) {

        annotationTokenFactory.setServer(wmRuntimeInfo.getWireMock(), wmRuntimeInfo.getHttpPort());
        ((ReactiveIssuerAuthenticationManagerResolver) authenticationManagerResolver)
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
            @PartnerTenant(clientId = "clientId", subject = "subject") String token) {

        webTestClient
                .get()
                .uri("/protected")
                .headers(
                        httpHeaders -> {
                            httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
                            httpHeaders.add("Authorization", token);
                        })
                .exchange()
                .expectStatus()
                .isOk();
    }

    @AfterAll
    static void aferAllTests(TenantAnnotationTokenFactory annotationTokenFactory) {
        annotationTokenFactory.close();
    }
}
