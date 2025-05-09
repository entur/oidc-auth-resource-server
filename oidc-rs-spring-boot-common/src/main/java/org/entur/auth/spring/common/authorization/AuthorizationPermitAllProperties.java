package org.entur.auth.spring.common.authorization;

import lombok.Data;

@Data
public class AuthorizationPermitAllProperties {
    private boolean actuator = true;
    private boolean openApi = true;

    private AuthorizationMatcherProperties matcher = new AuthorizationMatcherProperties();
}
