package org.entur.auth.spring.config.util;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtConverter {

    public static String getSubject (final Jwt jwt) {
        return jwt == null ? null : jwt.getSubject();
    }

    public static String getSubject (Authentication authentication) {
        if(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return getSubject(jwtAuthenticationToken.getToken());
        } else {
            return null;
        }
    }

    public static String getClientId (final Jwt jwt) {
        return jwt == null ? null : jwt.getClaimAsString("azp");
    }

    public static String getClientId (Authentication authentication) {
        if(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return getClientId(jwtAuthenticationToken.getToken());
        } else {
            return null;
        }
    }

    public static Long getOrganisationID (final Jwt jwt) {
        return jwt == null ? null : jwt.getClaim("https://entur.io/organisationID");
    }

    public static Long getOrganisationID (Authentication authentication) {
        if(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return getOrganisationID(jwtAuthenticationToken.getToken());
        } else {
            return null;
        }
    }

    public static String getEmail (final Jwt jwt) {
        return jwt == null ? null : jwt.getClaimAsString("email");
    }

    public static Boolean getEmailVerified (final Jwt jwt) {
        return jwt == null ? null : jwt.getClaimAsBoolean("email_verified");
    }

    public static String getUsername (final Jwt jwt) {
        return jwt == null ? null : jwt.getClaimAsString("preferred_username");
    }

    public static Jwt getJwt(@NonNull String accessToken) throws ParseException {
        JWT jwt = JWTParser.parse(accessToken);
        Map<String, Object> headers = new HashMap<>();
        headers.put("typ", jwt.getHeader().getType().getType());
        return new Jwt(accessToken, Instant.now(), Instant.now().plusSeconds(1000), headers, jwt.getJWTClaimsSet().getClaims());
    }
}
