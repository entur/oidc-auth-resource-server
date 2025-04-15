package org.entur.auth.spring.config;

import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class, TenantJsonWebToken.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "entur.auth.cors.mode=default")
@AutoConfigureMockMvc
class CorsDefaultHostTest {

    @Autowired
    protected MockMvc mockMvc;

    private final List<HttpMethod> methods = List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);
    private final List<String> hosts = List.of("http://unknown.host");

    @Test
    void testCorsOptionsAllowed() {
        hosts.forEach(host -> methods.forEach(method -> {
            var requestHeaders = new HttpHeaders();
            requestHeaders.add("Origin", host);
            try {
                mockMvc.perform(request(method, "/unprotected").headers(requestHeaders))
                        .andExpect(status().isOk());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));
    }
}
