package org.entur.auth.spring.test.other;

import org.entur.auth.junit.tenant.InternalTenant;
import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith({SpringExtension.class, TenantJsonWebToken.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LifecycleClassTest {
    @Autowired private WebTestClient webTestClient;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        TenantJsonWebToken.setupTokenFactory();
    }

    @Test
    void testInternal(@InternalTenant(clientId = "clientId") String authorization) {
        webTestClient
                .get()
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
