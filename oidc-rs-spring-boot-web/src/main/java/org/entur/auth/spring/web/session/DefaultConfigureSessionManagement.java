package org.entur.auth.spring.web.session;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

public class DefaultConfigureSessionManagement implements ConfigureSessionManagement {
    @Override
    public void customize(SessionManagementConfigurer<HttpSecurity> sessionManagementConfigurer) {
        sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
