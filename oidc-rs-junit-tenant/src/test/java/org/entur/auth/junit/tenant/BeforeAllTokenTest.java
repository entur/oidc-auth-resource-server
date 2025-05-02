package org.entur.auth.junit.tenant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TenantJsonWebToken.class)
class BeforeAllTokenTest {
    private static String token;

    @BeforeAll
    static void BeforeAllTest(@PartnerTenant(clientId = "abc", username = "testUser") String token) {
        BeforeAllTokenTest.token = token;
    }

    @Test
    void testTokenExists() {
        Assertions.assertNotNull(token);
    }
}
