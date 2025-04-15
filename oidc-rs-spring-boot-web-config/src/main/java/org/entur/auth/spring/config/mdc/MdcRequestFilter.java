package org.entur.auth.spring.config.mdc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.web.mdc.ConfigureMdcRequestFilter;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MdcRequestFilter extends OncePerRequestFilter implements ConfigureMdcRequestFilter {
    private final List<MdcFromToProperties> mappings = new ArrayList<>();

    public MdcRequestFilter(MdcProperties mdcProperties) {
        this.mappings.addAll(mdcProperties.getMappings());
        if(this.mappings.isEmpty()) {
            this.mappings.add(new MdcFromToProperties("azp", "clientId"));
            this.mappings.add(new MdcFromToProperties("https://entur.io/organisationID", "organisationId"));
        }
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            Jwt token = jwtAuthenticationToken.getToken();
            addMDC(mappings, token);
            try {
                chain.doFilter(request, response);
            } finally {
                removeMDC(mappings);
            }
        } else {
            chain.doFilter(request,response);
        }
    }

    private static void addMDC(@NonNull List<MdcFromToProperties> mappings, Jwt token) {
        if(token == null) {
            return;
        }

        mappings.forEach(mdcFromToProperties -> {
            Object value = token.getClaim(mdcFromToProperties.getFrom());
            if (value != null) {
                MDC.put(mdcFromToProperties.getTo(), value.toString());
            }
        });
    }

    private static void removeMDC(@NonNull List<MdcFromToProperties> mappings) {
        mappings.forEach(mdcFromToProperties -> MDC.remove(mdcFromToProperties.getTo()));
    }
}