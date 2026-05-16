package yowyob.comops.api.organization.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OrganizationServiceSubscriptionQuotaProperties.class)
public class OrganizationModuleConfiguration {
}
