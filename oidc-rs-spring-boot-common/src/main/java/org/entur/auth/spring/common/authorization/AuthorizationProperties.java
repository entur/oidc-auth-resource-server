package org.entur.auth.spring.common.authorization;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "entur.auth.authorization")
public class AuthorizationProperties {
    private AuthorizationPermitAllProperties permitAll = new AuthorizationPermitAllProperties();
}
