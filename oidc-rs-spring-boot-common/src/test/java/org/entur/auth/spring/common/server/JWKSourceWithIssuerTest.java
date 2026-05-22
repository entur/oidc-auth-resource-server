package org.entur.auth.spring.common.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.jwk.source.JWKSetBasedJWKSource;
import com.nimbusds.jose.jwk.source.JWKSetSource;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWKSourceWithIssuer test suite")
class JWKSourceWithIssuerTest {
    @Mock private JWKSetBasedJWKSource<SecurityContext> jwkSetBasedJWKSource;

    @Mock private JWKSource<SecurityContext> jwkSource;

    @Mock private JWKSetSource<SecurityContext> jwkSetSource;

    @Nested
    @DisplayName("JWKSourceWithIssuer::getJWKSetSource test suite")
    class GetJWKSetSourceTests {
        @Test
        void should_return_jwk_set_based_jwk_source_if_supported() {
            when(jwkSetBasedJWKSource.getJWKSetSource()).thenReturn(jwkSetSource);

            assertThat(new JWKSourceWithIssuer<>("<url>", jwkSetBasedJWKSource).getJWKSetSource())
                    .isEqualTo(jwkSetSource);
        }

        @Test
        void should_fail_if_not_supported() {
            assertThatThrownBy(() -> new JWKSourceWithIssuer<>("<url>", jwkSource).getJWKSetSource())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(
                            "Not an instance of %s".formatted(JWKSetBasedJWKSource.class.getSimpleName()));
        }
    }
}
