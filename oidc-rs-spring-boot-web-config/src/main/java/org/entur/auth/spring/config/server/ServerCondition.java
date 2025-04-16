package org.entur.auth.spring.config.server;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ServerCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var tenantsProperties = Binder.get(context.getEnvironment())
                .bind("entur.auth.tenant", TenantsProperties.class)
                .orElse(null);
        var issuersProperties = Binder.get(context.getEnvironment())
                .bind("entur.auth.issuers", IssuersProperties.class)
                .orElse(null);
        return (tenantsProperties != null && tenantsProperties.isEnabled()) || (issuersProperties != null && !issuersProperties.isEmpty());
    }
}