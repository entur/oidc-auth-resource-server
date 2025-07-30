package org.entur.auth.spring.common.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssuerProperties {
    private String issuerUrl;
    private String certificateUrl;
    private Boolean retryOnFailure;
    private Integer jwksThrottleWait;
    private Integer refreshAheadTime;
    private Integer cacheRefreshTimeout;
    private Integer cacheLifespan;
    private Integer outageTolerant;
}
