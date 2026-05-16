package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.application.port.in.OrganizationServiceEntitlements;
import java.util.List;
import java.util.UUID;

public record OrganizationServicesResponse(
        UUID organizationId,
        List<String> subscribedServices,
        List<String> effectiveServices,
        List<OrganizationServiceQuotaResponse> serviceQuotas,
        List<OrganizationServiceDependencyIssueResponse> dependencyIssues) {

    public static OrganizationServicesResponse from(OrganizationServiceEntitlements entitlements) {
        return new OrganizationServicesResponse(entitlements.organizationId(), entitlements.subscribedServices(),
                entitlements.effectiveServices(),
                entitlements.serviceQuotas().stream().map(OrganizationServiceQuotaResponse::from).toList(),
                entitlements.dependencyIssues().stream().map(OrganizationServiceDependencyIssueResponse::from).toList());
    }
}
