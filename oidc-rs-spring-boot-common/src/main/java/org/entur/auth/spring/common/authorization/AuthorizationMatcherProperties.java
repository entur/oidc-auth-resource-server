package org.entur.auth.spring.common.authorization;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "entur.auth.authorization.permitAll.matcher")
public class AuthorizationMatcherProperties {
    private List<String> patterns = new ArrayList<>();
    private AuthorizationMethodMatcherProperties method = new AuthorizationMethodMatcherProperties();

    public String[] getPatternsAsArray() {
        return patterns.toArray(new String[0]);
    }
}
