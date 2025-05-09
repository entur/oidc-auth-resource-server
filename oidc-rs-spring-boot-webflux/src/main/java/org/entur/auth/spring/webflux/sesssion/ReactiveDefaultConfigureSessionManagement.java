package org.entur.auth.spring.webflux.sesssion;

import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

public class ReactiveDefaultConfigureSessionManagement
        implements ReactiveConfigureSessionManagement {
    @Override
    public void customize(ServerHttpSecurity.RequestCacheSpec requestCacheSpec) {
        requestCacheSpec.requestCache(NoOpServerRequestCache.getInstance());
    }
}
