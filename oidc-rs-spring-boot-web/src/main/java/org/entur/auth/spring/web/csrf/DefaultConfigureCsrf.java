package org.entur.auth.spring.web.csrf;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;

public class DefaultConfigureCsrf implements ConfigureCsrf {
    @Override
    public void customize(CsrfConfigurer<HttpSecurity> csrfConfigurer) {
        csrfConfigurer.disable();
    }
}
