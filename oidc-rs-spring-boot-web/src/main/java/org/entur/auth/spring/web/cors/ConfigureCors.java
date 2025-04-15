package org.entur.auth.spring.web.cors;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;

public interface ConfigureCors extends Customizer<CorsConfigurer<HttpSecurity>> {
}
