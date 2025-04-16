package org.entur.auth.spring.config.server;

import lombok.Data;

@Data
public class IssuerProperties {
    private String issuerUrl;
    private String certificateUrl;
    private String authProvider;
    private Integer certificateReloadPeriodInMinutes = 60;
    private long fallbackCertificateReloadPeriodInMinutes;
    private Integer cacheLifespan = -1;
}
