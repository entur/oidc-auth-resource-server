package org.entur.auth.spring.common.server;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "entur.auth.tenants")
public class TenantsProperties {
    private String environment = "";
    private List<String> include = new ArrayList<>();
}
