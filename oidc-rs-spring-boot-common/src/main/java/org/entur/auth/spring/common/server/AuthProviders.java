package org.entur.auth.spring.common.server;

import java.util.List;

public interface AuthProviders {
    List<IssuerProperties> get(String environment, List<String> includeTenants);

    String getTenant(String authority);
}
