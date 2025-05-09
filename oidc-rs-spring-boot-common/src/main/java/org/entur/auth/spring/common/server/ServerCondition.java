package org.entur.auth.spring.common.server;

import lombok.NonNull;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ServerCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        var enturAuthProperties =
                Binder.get(context.getEnvironment())
                        .bind("entur.auth", EnturAuthProperties.class)
                        .orElse(null);
        return (enturAuthProperties != null && enturAuthProperties.isEnabled());
    }
}
