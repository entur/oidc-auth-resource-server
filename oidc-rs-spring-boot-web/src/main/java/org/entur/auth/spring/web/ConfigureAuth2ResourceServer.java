package org.entur.auth.spring.web;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

public interface ConfigureAuth2ResourceServer extends Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>> {
}
