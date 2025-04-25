package org.entur.auth.spring.config.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "entur.auth")
@Data
public class EnturAuthProperties {
    private boolean enabled = true;
    private boolean lazyLoad = false;
    private boolean retryOnFailure = false;
    private Integer connectTimeoutInSeconds = 3;
    private Integer readTimeoutInSeconds = 3;
    private Integer maxWaitingClients = 20;
    private Integer jwksThrottleWait = 60;

    private List<IssuerProperties> issuers = new ArrayList<>();
    private TenantsProperties tenants = new TenantsProperties();
    private List<ApiProperties> apis = new ArrayList<>();
}
