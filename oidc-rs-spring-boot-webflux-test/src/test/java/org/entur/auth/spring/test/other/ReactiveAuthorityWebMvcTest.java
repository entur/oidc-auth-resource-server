package org.entur.auth.spring.test.other;

import org.entur.auth.junit.tenant.InternalTenant;
import org.entur.auth.junit.tenant.PartnerTenant;
import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.entur.auth.spring.application.GreetingController;
import org.entur.auth.spring.config.ConfigReactiveAuthManagerResolverAutoConfiguration;
import org.entur.auth.spring.config.ConfigReactiveAuthProvidersAutoConfiguration;
import org.entur.auth.spring.config.ConfigReactiveResourceServerAutoConfiguration;
import org.entur.auth.spring.webflux.ReactiveResourceServerAutoConfiguration;
import org.entur.auth.spring.webflux.ReactiveResourceServerDefaultAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@TestPropertySource(
        properties = {
            "entur.auth.lazy-load=true",
            "entur.auth.tenants.environment=mock",
            "entur.auth.tenants.include=internal,partner",
        })
@ExtendWith({TenantJsonWebToken.class})
@WebFluxTest({GreetingController.class})
@ImportAutoConfiguration({
    ReactiveResourceServerAutoConfiguration.class,
    ReactiveResourceServerDefaultAutoConfiguration.class,
    ConfigReactiveAuthProvidersAutoConfiguration.class,
    ConfigReactiveAuthManagerResolverAutoConfiguration.class,
    ConfigReactiveResourceServerAutoConfiguration.class
})
public class ReactiveAuthorityWebMvcTest {
    @Autowired private WebTestClient webTestClient;

    @Test
    void testInternalWithPartner(
            @PartnerTenant(clientId = "clientId", subject = "subject") String authorization) {
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
                .isForbidden();
    }

    @Test
    void testInternalWithInternal(@InternalTenant(clientId = "clientId") String authorization) {
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
