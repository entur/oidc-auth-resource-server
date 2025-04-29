package org.entur.auth.spring.config.cors;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "entur.auth.cors")
public class CorsProperties {
    private String mode;
    private List<String> hosts = new ArrayList<>();
}
