package org.entur.auth.spring.test.cors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {"entur.auth.cors.mode=api", "entur.auth.cors.hosts=http://known.host"})
@AutoConfigureMockMvc
class CorsAPIHostTest {
    private final List<HttpMethod> methods =
            List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);
    private final List<String> hosts =
            List.of(
                    "https://petstore.swagger.io",
                    "https://test-entur.devportal.apigee.io",
                    "https://developer.entur.org",
                    "http://known.host");

    @Autowired protected MockMvc mockMvc;

    @Test
    void testCorsHostAndMethodAllowed() {
        hosts.forEach(
                host ->
                        methods.forEach(
                                method -> {
                                    var requestHeaders = new HttpHeaders();
                                    requestHeaders.add("Origin", host);
                                    try {
                                        mockMvc
                                                .perform(request(method, "/unprotected").headers(requestHeaders))
                                                .andExpect(status().isOk());
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }));
    }

    @Test
    void testCorsHostNotAllowed() throws Exception {
        var requestHeaders = new HttpHeaders();
        requestHeaders.add("Origin", "http://unknown.host");
        mockMvc.perform(get("/unprotected").headers(requestHeaders)).andExpect(status().isForbidden());
    }
}
