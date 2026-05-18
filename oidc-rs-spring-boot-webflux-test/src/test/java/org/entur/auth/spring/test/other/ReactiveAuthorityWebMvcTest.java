package org.entur.auth.spring.test.other;

import static org.springframework.http.HttpMethod.GET;

import org.entur.auth.junit.tenant.InternalTenant;
import org.entur.auth.junit.tenant.PartnerTenant;
import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@TestPropertySource(
        properties = {
            "entur.auth.lazy-load=true",
            "entur.auth.tenants.environment=mock",
            "entur.auth.tenants.include=internal,partner",
        })
@SpringBootTest
@ExtendWith({TenantJsonWebToken.class})
@AutoConfigureWebTestClient
public class ReactiveAuthorityWebMvcTest {
    @Autowired private WebTestClient webTestClient;

    @Test
    void testInternalWithPartner(
            @PartnerTenant(clientId = "clientId", subject = "subject") String authorization) {
        webTestClient
                .method(GET)
                .uri("/internal")
                .headers(
                        httpHeaders -> {
                            httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
                            httpHeaders.add("Authorization", authorization);
                        })
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void testInternalWithInternal(@InternalTenant(clientId = "clientId") String authorization) {
        webTestClient
                .method(GET)
                .uri("/internal")
                .headers(
                        httpHeaders -> {
                            httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
                            httpHeaders.add("Authorization", authorization);
                        })
                .exchange()
                .expectStatus()
                .isOk();
    }
}
