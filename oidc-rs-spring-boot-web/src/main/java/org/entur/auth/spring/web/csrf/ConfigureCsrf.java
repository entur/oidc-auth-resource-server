package org.entur.auth.spring.web.csrf;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;

public interface ConfigureCsrf extends Customizer<CsrfConfigurer<HttpSecurity>> {}
