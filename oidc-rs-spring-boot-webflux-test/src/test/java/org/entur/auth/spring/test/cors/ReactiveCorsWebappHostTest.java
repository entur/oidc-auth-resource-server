package org.entur.auth.spring.test.cors;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(
        properties = {"entur.auth.cors.mode=webapp", "entur.auth.cors.hosts=http://known.host"})
class ReactiveCorsWebappHostTest {
    private final List<HttpMethod> methods =
            List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);
    private final List<String> hosts =
            List.of(
                    "https://petstore.swagger.io",
                    "https://test-entur.devportal.apigee.io",
                    "https://developer.entur.org");

    @LocalServerPort private int randomServerPort;

    @Autowired private WebTestClient webTestClient;

    @Test
    void testCorsHostForbidden() {
        hosts.forEach(
                host ->
                        methods.forEach(
                                method ->
                                        webTestClient
                                                .method(method)
                                                .uri("http://localhost:" + randomServerPort + "/unprotected")
                                                .headers(httpHeaders -> httpHeaders.add("Origin", host))
                                                .exchange()
                                                .expectStatus()
                                                .isForbidden()));
    }

    @Test
    void testCorsHostAllowed() {
        webTestClient
                .get()
                .uri("http://localhost:" + randomServerPort + "/unprotected")
                .headers(httpHeaders -> httpHeaders.add("Origin", "http://known.host"))
                .exchange()
                .expectStatus()
                .isOk();
    }
}
