package org.entur.auth.spring.test.authorization;

import static com.google.common.truth.Truth.assertThat;

import org.entur.auth.junit.tenant.PartnerTenant;
import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, TenantJsonWebToken.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthorizeRequestsTemplateTest {

    @LocalServerPort private int randomServerPort;

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void testProtectedWithPartner(
            @PartnerTenant(clientId = "clientId", subject = "subject") String token) {
        var requestHeaders = new HttpHeaders();
        requestHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.add("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(requestHeaders);

        String url = "http://localhost:" + randomServerPort + "/protected";

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
