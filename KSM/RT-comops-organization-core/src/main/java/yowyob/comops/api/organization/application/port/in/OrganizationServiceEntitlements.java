package yowyob.comops.api.organization.application.port.in;

import java.util.List;
import java.util.UUID;

public record OrganizationServiceEntitlements(
        UUID organizationId,
        List<String> subscribedServices,
        List<String> effectiveServices,
        List<OrganizationServiceQuota> serviceQuotas) {
}
