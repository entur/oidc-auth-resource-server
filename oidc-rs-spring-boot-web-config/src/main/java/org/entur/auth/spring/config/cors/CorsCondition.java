package org.entur.auth.spring.config.cors;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class CorsCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var properties =
                Binder.get(context.getEnvironment())
                        .bind("entur.auth.cors", CorsProperties.class)
                        .orElse(null);
        return properties != null;
    }
}
