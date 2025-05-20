package org.entur.auth.spring.config;

import org.entur.auth.spring.common.server.ServerExternalCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Conditional(ServerExternalCondition.class)
@ConditionalOnProperty(name = {"entur.auth.external.resource"})
@PropertySource(value = "${entur.auth.external.resource}")
public class ConfigReactiveExternalPropertyAutoConfiguration {}
