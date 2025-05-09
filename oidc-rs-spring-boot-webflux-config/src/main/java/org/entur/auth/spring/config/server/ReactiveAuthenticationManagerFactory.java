package org.entur.auth.spring.config.server;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.source.JWKSetSourceWithHealthStatusReporting;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWKSecurityContext;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.health.HealthReportListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.common.server.AudienceValidator;
import org.entur.auth.spring.common.server.EnturAuthProperties;
import org.entur.auth.spring.common.server.IssuerProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReactiveAuthenticationManagerFactory {
    public static void add(
            Map<String, ReactiveAuthenticationManager> authenticationManagers,
            List<ReactiveJWKSourceWithIssuer> remoteJWKSets,
            IssuerProperties provider,
            @NonNull EnturAuthProperties enturAuthProperties,
            @NonNull Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter,
            HealthReportListener<JWKSetSourceWithHealthStatusReporting<SecurityContext>, SecurityContext>
                    healthReportListener) {

        long cacheLifespan =
                (provider.getCacheLifespan() != null
                        ? provider.getCacheLifespan()
                        : enturAuthProperties.getCacheLifespan());
        long cacheRefreshTimeout =
                (provider.getCacheRefreshTimeout() != null
                        ? provider.getCacheRefreshTimeout()
                        : enturAuthProperties.getCacheRefreshTimeout());
        long refreshAheadTime =
                (provider.getRefreshAheadTime() != null
                        ? provider.getRefreshAheadTime()
                        : enturAuthProperties.getRefreshAheadTime());
        long jwksThrottleWait =
                (provider.getJwksThrottleWait() != null
                        ? provider.getJwksThrottleWait()
                        : enturAuthProperties.getJwksThrottleWait());
        boolean retryOnFailure =
                provider.getRetryOnFailure() != null
                        ? provider.getRetryOnFailure()
                        : enturAuthProperties.isRetryOnFailure();

        try {
            final JWKSourceBuilder<SecurityContext> jwkSourceBuilder;
            var resourceRetriever =
                    new DefaultResourceRetriever(
                            enturAuthProperties.getConnectTimeout() * 1000,
                            enturAuthProperties.getReadTimeout() * 1000);
            if (cacheLifespan <= 0) {
                jwkSourceBuilder =
                        JWKSourceBuilder.create(new URL(provider.getCertificateUrl()), resourceRetriever)
                                .cache(Long.MAX_VALUE, cacheRefreshTimeout * 1000)
                                .refreshAheadCache(false)
                                .rateLimited(jwksThrottleWait * 1000)
                                .retrying(retryOnFailure)
                                .outageTolerant(false);
            } else {
                jwkSourceBuilder =
                        JWKSourceBuilder.create(new URL(provider.getCertificateUrl()), resourceRetriever)
                                .cache(cacheLifespan * 1000, cacheRefreshTimeout * 1000)
                                .refreshAheadCache(refreshAheadTime * 1000, true)
                                .rateLimited(jwksThrottleWait * 1000)
                                .retrying(retryOnFailure)
                                .outageTolerantForever();
            }

            if (healthReportListener != null) {
                jwkSourceBuilder.healthReporting(healthReportListener);
            }

            var jwkSource =
                    new ReactiveJWKSourceWithIssuer(provider.getIssuerUrl(), jwkSourceBuilder.build());
            remoteJWKSets.add(jwkSource);

            // Create selector
            final JWSVerificationKeySelector<JWKSecurityContext> keySelector;
            if (Boolean.TRUE.equals(enturAuthProperties.getLazyLoad())) {
                log.info("Notice: Lazy load of JWKS only supports RSA algorithms");
                keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.Family.RSA, jwkSource);
            } else {
                keySelector = fromJWKSource(jwkSource);
            }

            NimbusReactiveJwtDecoder jwtDecoder =
                    createNimbusJwtDecoder(enturAuthProperties, provider, keySelector);
            JwtReactiveAuthenticationManager authenticationProvider =
                    createJwtAuthenticationProvider(jwtGrantedAuthoritiesConverter, jwtDecoder);

            // Add AuthenticationManager to map
            authenticationManagers.put(provider.getIssuerUrl(), authenticationProvider);

            log.info(
                    "Added authorization server: issuerUri = {}, cacheLifespan:  = {}, cacheRefreshTimeout = {}, refreshAheadTime = {}, jwksThrottleWait = {}, lazyLoad = {}, certificateUrl = {}",
                    provider.getIssuerUrl(),
                    cacheLifespan,
                    cacheRefreshTimeout,
                    refreshAheadTime,
                    jwksThrottleWait,
                    enturAuthProperties.getLazyLoad(),
                    provider.getCertificateUrl());
        } catch (RuntimeException | KeySourceException | MalformedURLException ex) {
            log.error(
                    "Exception adding authorization server: issuerUri = {}, cacheLifespan:  = {}, cacheRefreshTimeout = {}, refreshAheadTime = {}, jwksThrottleWait = {}, lazyLoad = {}, certificateUrl = {}",
                    provider.getIssuerUrl(),
                    cacheLifespan,
                    cacheRefreshTimeout,
                    refreshAheadTime,
                    jwksThrottleWait,
                    enturAuthProperties.getLazyLoad(),
                    provider.getCertificateUrl(),
                    ex);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private static <C extends SecurityContext> JWSVerificationKeySelector<C> fromJWKSource(
            final JWKSource<C> jwkSource) throws KeySourceException {

        JWKMatcher jwkMatcher =
                new JWKMatcher.Builder()
                        .publicOnly(true)
                        .keyUses(KeyUse.SIGNATURE, null) // use=sig is optional
                        .keyTypes(KeyType.RSA, KeyType.EC)
                        .build();
        List<? extends JWK> jwks = jwkSource.get(new JWKSelector(jwkMatcher), null);
        for (JWK jwk : jwks) {
            if (KeyType.RSA.equals(jwk.getKeyType())) {
                return new JWSVerificationKeySelector<>(JWSAlgorithm.Family.RSA, jwkSource);
            }
            if (KeyType.EC.equals(jwk.getKeyType())) {
                return new JWSVerificationKeySelector<>(JWSAlgorithm.Family.EC, jwkSource);
            }
        }
        throw new KeySourceException("Couldn't retrieve JWKs");
    }

    private static JwtReactiveAuthenticationManager createJwtAuthenticationProvider(
            Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter,
            ReactiveJwtDecoder decoder) {
        // Create AuthenticationManager and add to map
        JwtReactiveAuthenticationManager authenticationManager =
                new JwtReactiveAuthenticationManager(decoder);

        // AuthenticationConverter
        if (jwtGrantedAuthoritiesConverter != null) {
            JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
            jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
            authenticationManager.setJwtAuthenticationConverter(
                    new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter));
        }
        return authenticationManager;
    }

    private static NimbusReactiveJwtDecoder createNimbusJwtDecoder(
            EnturAuthProperties enturAuthProperties,
            IssuerProperties provider,
            JWSVerificationKeySelector<JWKSecurityContext> keySelector) {

        NimbusReactiveJwtDecoder jwtDecoder =
                NimbusReactiveJwtDecoder.withJwkSetUri(provider.getCertificateUrl())
                        .jwtProcessorCustomizer(processor -> processor.setJWSKeySelector(keySelector))
                        .build();
        var apis = enturAuthProperties.getApis();
        var audiences =
                apis.stream()
                        .filter(
                                apiProperties ->
                                        Objects.equals(provider.getIssuerUrl(), apiProperties.getIssuerUrl()))
                        .flatMap(apiProperties -> apiProperties.getAudiences().stream())
                        .collect(Collectors.toSet());

        if (!audiences.isEmpty()) {
            OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audiences);
            OAuth2TokenValidator<Jwt> withIssuer =
                    JwtValidators.createDefaultWithIssuer(provider.getIssuerUrl());
            OAuth2TokenValidator<Jwt> withAudience =
                    new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
            jwtDecoder.setJwtValidator(withAudience);
        }
        return jwtDecoder;
    }
}
