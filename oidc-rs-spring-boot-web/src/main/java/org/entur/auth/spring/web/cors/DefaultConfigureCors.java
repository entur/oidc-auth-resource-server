package org.entur.auth.spring.web.cors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;

@Slf4j
public class DefaultConfigureCors implements ConfigureCors {
    @Override
    public void customize(CorsConfigurer<HttpSecurity> httpSecurityCorsConfigurer) {
        log.info("Configure DefaultConfigureCors");
        Customizer.withDefaults().customize(httpSecurityCorsConfigurer);
    }
}
