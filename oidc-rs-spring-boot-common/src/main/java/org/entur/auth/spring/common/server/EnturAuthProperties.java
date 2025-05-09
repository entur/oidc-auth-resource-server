package org.entur.auth.spring.common.server;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "entur.auth")
@Data
public class EnturAuthProperties {
    private boolean enabled = true;
    private Boolean lazyLoad;
    private boolean retryOnFailure = false;
    private int connectTimeout = 5;
    private int readTimeout = 5;
    private int jwksThrottleWait = 30;
    private int refreshAheadTime = 30;
    private int cacheRefreshTimeout = 15;
    private int cacheLifespan = 300;

    private List<IssuerProperties> issuers = new ArrayList<>();
    private TenantsProperties tenants = new TenantsProperties();
    private List<ApiProperties> apis = new ArrayList<>();
}
