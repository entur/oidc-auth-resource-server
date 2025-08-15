package org.entur.auth.spring.test.other;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.entur.auth.junit.tenant.InternalTenant;
import org.entur.auth.junit.tenant.PartnerTenant;
import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.entur.auth.spring.application.GreetingController;
import org.entur.auth.spring.config.ConfigAuthManagerResolverAutoConfiguration;
import org.entur.auth.spring.config.ConfigAuthProvidersAutoConfiguration;
import org.entur.auth.spring.config.ConfigResourceServerAutoConfiguration;
import org.entur.auth.spring.web.ResourceServerAutoConfiguration;
import org.entur.auth.spring.web.ResourceServerDefaultAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@TestPropertySource(
        properties = {
            "entur.auth.lazy-load=true",
            "entur.auth.tenants.environment=mock",
            "entur.auth.tenants.include=internal,partner",
        })
@ExtendWith({TenantJsonWebToken.class})
@WebMvcTest({GreetingController.class})
@ImportAutoConfiguration({
    ResourceServerAutoConfiguration.class,
    ResourceServerDefaultAutoConfiguration.class,
    ConfigAuthProvidersAutoConfiguration.class,
    ConfigAuthManagerResolverAutoConfiguration.class,
    ConfigResourceServerAutoConfiguration.class
})
public class AuthorityWebMvcTest {
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
