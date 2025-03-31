package org.entur.auth.spring.web;

import org.entur.auth.junit.tenant.PartnerTenant;
import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({MockitoExtension.class, TenantJsonWebToken.class})
public class EchoTest {
    @Autowired
    protected MockMvc mockMvc;

    @Test
    void testEchoUnauthorized() throws Exception {
        mockMvc.perform(get("/echo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testEchoOk(@PartnerTenant(clientId = "clientId", subject = "noAccess") String token) throws Exception {
        var requestHeaders = new HttpHeaders();
        requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.add("Authorization", token);

        mockMvc.perform(get("/echo").headers(requestHeaders))
                .andExpect(status().isOk());
    }
}
