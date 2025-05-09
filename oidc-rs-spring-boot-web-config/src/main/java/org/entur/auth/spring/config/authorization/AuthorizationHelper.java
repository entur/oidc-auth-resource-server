package org.entur.auth.spring.config.authorization;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.common.authorization.AuthorizationHttpMethodMatcherProperties;
import org.entur.auth.spring.common.authorization.AuthorizationMatcherProperties;
import org.entur.auth.spring.common.authorization.AuthorizationPermitAllProperties;
import org.entur.auth.spring.common.authorization.AuthorizationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class AuthorizationHelper {
    public static void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
                    authorizeRequests,
            AuthorizationProperties authorization,
            String managementBasePath) {

        AuthorizationPermitAllProperties permitAll = authorization.getPermitAll();

        if (permitAll.isActuator() && managementBasePath != null) {
            authorizeRequests.requestMatchers(managementBasePath + "/**").permitAll();
            log.info("All authorize requests to {}/** will be permitted", managementBasePath);
        }

        if (permitAll.isOpenApi()) {
            authorizeRequests.requestMatchers(HttpMethod.GET, "/v2/api-docs", "/v3/api-docs").permitAll();
            log.info("All authorize requests to /v2/api-docs and /v3/api-docs will be permitted");
        }

        configurePermitAllMatchers(authorizeRequests, permitAll.getMatcher());

        authorizeRequests.anyRequest().fullyAuthenticated();
    }

    private static void configurePermitAllMatchers(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
                    authorizeRequests,
            AuthorizationMatcherProperties matchers) {
        authorizeRequests.requestMatchers(matchers.getPatternsAsArray()).permitAll();
        matchers
                .getPatterns()
                .forEach(pattern -> log.info("All authorize requests to {} will be permitted", pattern));

        for (AuthorizationHttpMethodMatcherProperties httpMethodMatcher :
                matchers.getMethod().getActiveMethods()) {
            if (httpMethodMatcher.getPatternsAsArray().length > 0) {
                authorizeRequests
                        .requestMatchers(httpMethodMatcher.getVerb(), httpMethodMatcher.getPatternsAsArray())
                        .permitAll();
            }
        }
    }
}
