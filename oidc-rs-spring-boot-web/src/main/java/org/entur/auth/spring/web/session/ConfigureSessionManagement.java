package org.entur.auth.spring.web.session;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;

public interface ConfigureSessionManagement
        extends Customizer<SessionManagementConfigurer<HttpSecurity>> {}
