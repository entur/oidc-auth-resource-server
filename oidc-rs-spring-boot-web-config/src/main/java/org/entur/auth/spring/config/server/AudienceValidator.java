package org.entur.auth.spring.config.server;

import lombok.NonNull;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Set;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    @NonNull
    private final Set<String> audiences;
    OAuth2Error error = new OAuth2Error("401", "Not valid audience", null);

    public AudienceValidator(Set<String> audiences) {
        this.audiences = audiences != null ? Set.copyOf(audiences) : Set.of();
    }

    /**
     * Verify the validity jwt token has one of the defined audiences.
     * @param jwt an Jwt token
     * @return OAuth2TokenValidationResult the success or failure detail of the validation
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        if(audiences.stream().anyMatch(audience -> jwt.getAudience() != null && jwt.getAudience().contains(audience))) {
            return OAuth2TokenValidatorResult.success();
        } else {
            return OAuth2TokenValidatorResult.failure(error);
        }
    }
}