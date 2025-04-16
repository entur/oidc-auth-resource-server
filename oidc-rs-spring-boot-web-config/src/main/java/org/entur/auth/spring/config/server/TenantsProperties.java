package org.entur.auth.spring.config.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "entur.auth.tenants")
public class TenantsProperties {
    private boolean enabled = true;
    private String environment = "";
    private List<String> include = new ArrayList<>();
}
