package org.entur.auth.spring.config.server;

import lombok.Data;

@Data
public class IssuerProperties {
    private String issuerUrl;
    private String certificateUrl;
    private Boolean retryOnFailure;
    private Integer jwksThrottleWait;
    private Integer refreshAheadTime;
    private Integer cacheRefreshTimeout;
    private Integer cacheLifespan;
}
