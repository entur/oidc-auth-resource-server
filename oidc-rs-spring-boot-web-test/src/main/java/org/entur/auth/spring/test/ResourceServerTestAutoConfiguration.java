package org.entur.auth.spring.test;

import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.entur.auth.spring.common.server.EnturAuthProperties;
import org.entur.auth.spring.common.server.ServerCondition;
import org.entur.auth.spring.config.ResourceServerAutoConfiguration;
import org.entur.auth.spring.test.server.EnturAuthTestProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureBefore({ResourceServerAutoConfiguration.class})
public class ResourceServerTestAutoConfiguration {

    @Conditional(ServerCondition.class)
    @EnableConfigurationProperties({EnturAuthProperties.class, EnturAuthTestProperties.class})
    public static class ConfigureAuth2ResourceServerBean {
        public ConfigureAuth2ResourceServerBean(
                EnturAuthProperties enturAuthProperties, EnturAuthTestProperties enturAuthTestProperties) {

            if (!enturAuthTestProperties.isLoadEnvironments()
                    && !"mock".equals(enturAuthProperties.getTenants().getEnvironment())) {
                enturAuthProperties.getTenants().setEnvironment("");
            }

            if (!enturAuthTestProperties.isLoadIssuers()) {
                enturAuthProperties.setIssuers(Collections.emptyList());
            }

            if (enturAuthProperties.getLazyLoad() == null) {
                log.info("Turn on lazy load of JWKS to support use of TenantJsonWebToken.");
                enturAuthProperties.setLazyLoad(true);
            }
        }
    }
}
