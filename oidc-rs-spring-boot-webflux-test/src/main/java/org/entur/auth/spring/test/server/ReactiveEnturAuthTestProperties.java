package org.entur.auth.spring.test.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "entur.auth.test")
@Data
public class ReactiveEnturAuthTestProperties {
    private boolean loadEnvironments = false;
    private boolean loadIssuers = false;
}
