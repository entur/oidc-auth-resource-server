package org.entur.auth.spring.test.server;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.entur.auth.junit.tenant.InternalTenant;
import org.entur.auth.junit.tenant.PartnerTenant;
import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith({SpringExtension.class, TenantJsonWebToken.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthorityInternalTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void testInternalWithPartner(
            @PartnerTenant(clientId = "clientId", subject = "subject") String authorization)
            throws Exception {
        var requestHeaders = new HttpHeaders();
        requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.add("Authorization", authorization);

        mockMvc.perform(get("/internal").headers(requestHeaders)).andExpect(status().isForbidden());
    }

    @Test
    void testInternalWithInternal(@InternalTenant(clientId = "clientId") String authorization)
            throws Exception {
        var requestHeaders = new HttpHeaders();
        requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.add("Authorization", authorization);

        mockMvc.perform(get("/internal").headers(requestHeaders)).andExpect(status().isOk());
    }
}
