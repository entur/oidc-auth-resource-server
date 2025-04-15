package org.entur.auth.spring.config.authorization;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "entur.auth.authorization.permitAll.matcher")
public class AuthorizationMatcherProperties {
    private List<String> patterns = new ArrayList<>();
    private AuthorizationMethodMatcherProperties method = new AuthorizationMethodMatcherProperties();

    public String[] getPatternsAsArray() {
        return patterns.toArray(new String[0]);
    }
}
