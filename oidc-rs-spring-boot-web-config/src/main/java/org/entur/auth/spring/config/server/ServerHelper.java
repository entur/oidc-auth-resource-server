package org.entur.auth.spring.config.server;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;

public class ServerHelper {
    public static void configure(OAuth2ResourceServerConfigurer<HttpSecurity> configurer, TenantsProperties tenantsProperties, IssuersProperties issuerProperties) {
        //AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver = null;


        //configurer.authenticationManagerResolver(authenticationManagerResolver);
    }
}
