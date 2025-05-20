package org.entur.auth.spring.common.server;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "entur.auth.external")
@Data
public class ExternalAuthProperties {
    private boolean enabled = true;
    private List<String> tenants = new ArrayList<>();
    private List<IssuerProperties> issuers = new ArrayList<>();

    public List<IssuerProperties> getFilteredIssuers() {
        return issuers.stream().filter(this::filter).toList();
    }

    public boolean filter(@NonNull IssuerProperties issuers) {
        return tenants.stream()
                .anyMatch(item -> issuers.getIssuerUrl().contains(String.format("/%s", item)));
    }
}
