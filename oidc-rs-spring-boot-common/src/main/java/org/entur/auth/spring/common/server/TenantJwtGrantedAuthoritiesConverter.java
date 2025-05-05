package org.entur.auth.spring.common.server;

import java.util.ArrayList;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@RequiredArgsConstructor
public class TenantJwtGrantedAuthoritiesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();
    private final AuthProviders authProviders;

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt source) {
        var grantedAuthorities = jwtGrantedAuthoritiesConverter.convert(source);
        if (grantedAuthorities == null) {
            grantedAuthorities = new ArrayList<>();
        }

        var tenant = authProviders.getTenant(source.getIssuer().getAuthority());
        grantedAuthorities.add(new SimpleGrantedAuthority(tenant));

        return grantedAuthorities;
    }
}
