package org.entur.auth.spring.config.authorization;

import static org.springframework.http.HttpMethod.GET;

import org.entur.auth.junit.tenant.PartnerTenant;
import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("external")
@ExtendWith({SpringExtension.class, TenantJsonWebToken.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ReactiveAuthorizeRequestsExternalTest {
    @Autowired private WebTestClient webTestClient;

    @Test
    void testUnprotectedWithAnonymous() {
        webTestClient.get().uri("/unprotected").exchange().expectStatus().isOk();
    }

    @Test
    void testProtectedWithAnonymous() {
        webTestClient.get().uri("/protected").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void testProtectedWithPartner(
            @PartnerTenant(clientId = "clientId", subject = "subject") String token) {
        webTestClient
                .method(GET)
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
}
