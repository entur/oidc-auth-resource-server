package org.entur.auth.spring.test.cors;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CorsNoHostTest {
    private final List<HttpMethod> methods =
            List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);

    @Autowired private MockMvc mockMvc;

    @Test
    void testCorsHostAndMethodAllowed() {
        methods.forEach(
                method -> {
                    var requestHeaders = new HttpHeaders();
                    requestHeaders.add("Origin", "http://unknown.host");
                    try {
                        mockMvc
                                .perform(request(method, "/unprotected").headers(requestHeaders))
                                .andExpect(status().isOk());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
