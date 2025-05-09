package org.entur.auth.spring.config.authorization;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.common.authorization.AuthorizationHttpMethodMatcherProperties;
import org.entur.auth.spring.common.authorization.AuthorizationMatcherProperties;
import org.entur.auth.spring.common.authorization.AuthorizationPermitAllProperties;
import org.entur.auth.spring.common.authorization.AuthorizationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class ReactiveAuthorizationHelper {
    public static void configure(
            ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchangeSpec,
            AuthorizationProperties authorization,
            String managementBasePath) {

        AuthorizationPermitAllProperties permitAll = authorization.getPermitAll();

        if (permitAll.isActuator() && managementBasePath != null) {
            authorizeExchangeSpec.pathMatchers(managementBasePath + "/**").permitAll();
            log.info("All authorize requests to {}/** will be permitted", managementBasePath);
        }

        if (permitAll.isOpenApi()) {
            authorizeExchangeSpec
                    .pathMatchers(HttpMethod.GET, "/v2/api-docs", "/v3/api-docs")
                    .permitAll();
            log.info("All authorize requests to /v2/api-docs and /v3/api-docs will be permitted");
        }

        configurePermitAllMatchers(authorizeExchangeSpec, permitAll.getMatcher());

        authorizeExchangeSpec.anyExchange().authenticated();
    }

    private static void configurePermitAllMatchers(
            ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchangeSpec,
            AuthorizationMatcherProperties matchers) {
        authorizeExchangeSpec.pathMatchers(matchers.getPatternsAsArray()).permitAll();
        matchers
                .getPatterns()
                .forEach(pattern -> log.info("All authorize requests to {} will be permitted", pattern));

        for (AuthorizationHttpMethodMatcherProperties httpMethodMatcher :
                matchers.getMethod().getActiveMethods()) {
            if (httpMethodMatcher.getPatternsAsArray().length > 0) {
                authorizeExchangeSpec
                        .pathMatchers(httpMethodMatcher.getVerb(), httpMethodMatcher.getPatternsAsArray())
                        .permitAll();
            }
        }
    }
}
