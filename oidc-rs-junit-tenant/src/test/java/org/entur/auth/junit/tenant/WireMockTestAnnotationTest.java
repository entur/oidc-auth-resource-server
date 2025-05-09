package org.entur.auth.junit.tenant;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TenantJsonWebToken.class)
@WireMockTest
@Disabled
class WireMockTestAnnotationTest {

    @BeforeAll
    static void beforeAllTests(
            TenantAnnotationTokenFactory annotationTokenFactory, WireMockRuntimeInfo wmRuntimeInfo) {

        annotationTokenFactory.setServer(wmRuntimeInfo.getWireMock(), wmRuntimeInfo.getHttpPort());
    }

    @Test
    void testTokenExists(@PartnerTenant(clientId = "abc", username = "testUser") String token) {
        Assertions.assertNotNull(token);
    }

    @AfterAll
    static void aferAllTests(TenantAnnotationTokenFactory annotationTokenFactory) {
        annotationTokenFactory.shutdown();
    }
}
