package yowyob.comops.api.organization.application.port.in;

import java.util.List;
import java.util.UUID;

public record OrganizationCommercialSubscription(
        UUID organizationId,
        String planCode,
        List<String> addOnCodes,
        List<String> serviceCodes,
        List<CommercialServiceQuota> serviceQuotas,
        List<OrganizationServiceDependencyIssue> dependencyIssues,
        OrganizationServiceEntitlements entitlements) {
}
