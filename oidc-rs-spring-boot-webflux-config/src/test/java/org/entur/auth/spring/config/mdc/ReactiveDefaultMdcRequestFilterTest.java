package org.entur.auth.spring.config.mdc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.entur.auth.junit.tenant.PartnerTenant;
import org.entur.auth.junit.tenant.TenantJsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith({SpringExtension.class, TenantJsonWebToken.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ReactiveDefaultMdcRequestFilterTest {
    @Autowired private WebTestClient webTestClient;

    @Test
    void testDefaltMdcForPartner(
            @PartnerTenant(clientId = "clientId1234", subject = "subject", organisationId = 1234L)
                    String token) {

        Logger logger = (Logger) LoggerFactory.getLogger("org.entur.auth.spring.application");
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        webTestClient
                .get()
                .uri("/protected")
                .headers(
                        httpHeaders -> {
                            httpHeaders.add("Accept", MediaType.APPLICATION_JSON_VALUE);
                            httpHeaders.add("Authorization", token);
                        })
                .exchange()
                .expectStatus()
                .isOk();

        var logsList = listAppender.list;
        boolean foundClientId =
                logsList.stream()
                        .anyMatch(
                                event -> {
                                    String mdcValue = event.getMDCPropertyMap().get("clientId");
                                    return "clientId1234".equals(mdcValue);
                                });
        boolean foundOrganisationId =
                logsList.stream()
                        .anyMatch(
                                event -> {
                                    String mdcValue = event.getMDCPropertyMap().get("organisationId");
                                    return "1234".equals(mdcValue);
                                });
        assertTrue(foundClientId, "MDC clientId value not found in log events!");
        assertTrue(foundOrganisationId, "MDC organisationId value not found in log events!");
    }
}
