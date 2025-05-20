package org.entur.auth.spring.config.cors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.common.cors.CorsProperties;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
public class CorsHelper {
    public static void configure(
            CorsConfigurer<HttpSecurity> configurer, CorsProperties corsProperties) {
        if ("default".equals(corsProperties.getMode())) {
            configurer.configurationSource(getDefaultCorsConfiguration());
        } else if ("webapp".equals(corsProperties.getMode())) {
            configurer.configurationSource(getCorsConfiguration(corsProperties.getHosts()));
        } else if ("api".equals(corsProperties.getMode())) {
            configurer.configurationSource(
                    getCorsConfiguration(
                            Stream.concat(
                                            corsProperties.getHosts().stream(), Stream.of("https://petstore.swagger.io"))
                                    .toList()));
        }
    }

    private static CorsConfigurationSource getDefaultCorsConfiguration() {
        var config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setMaxAge(86400L);
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        log.info("Configure CORS rules with *");
        return source;
    }

    private static CorsConfigurationSource getCorsConfiguration(List<String> hosts) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(hosts);
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowedMethods(
                Arrays.asList("GET", "HEAD", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setMaxAge(86400L);
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        log.info("Configure CORS rules with hosts {}", hosts);
        return source;
    }
}
