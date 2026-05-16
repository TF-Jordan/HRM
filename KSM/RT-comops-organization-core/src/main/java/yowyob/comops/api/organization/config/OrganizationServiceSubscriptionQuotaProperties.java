package yowyob.comops.api.organization.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "iwm.organization.service-subscriptions")
public class OrganizationServiceSubscriptionQuotaProperties {

    private long defaultRequestQuotaLimit = 10000L;
    private Duration defaultRequestQuotaWindow = Duration.ofMinutes(1);
    private boolean provisionSubscribableServicesOnCreate;

    public long getDefaultRequestQuotaLimit() {
        return defaultRequestQuotaLimit;
    }

    public void setDefaultRequestQuotaLimit(long defaultRequestQuotaLimit) {
        this.defaultRequestQuotaLimit = defaultRequestQuotaLimit;
    }

    public Duration getDefaultRequestQuotaWindow() {
        return defaultRequestQuotaWindow;
    }

    public void setDefaultRequestQuotaWindow(Duration defaultRequestQuotaWindow) {
        this.defaultRequestQuotaWindow = defaultRequestQuotaWindow;
    }

    public boolean isProvisionSubscribableServicesOnCreate() {
        return provisionSubscribableServicesOnCreate;
    }

    public void setProvisionSubscribableServicesOnCreate(boolean provisionSubscribableServicesOnCreate) {
        this.provisionSubscribableServicesOnCreate = provisionSubscribableServicesOnCreate;
    }
}
