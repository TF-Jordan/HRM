package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.application.port.in.OrganizationCommercialSubscription;
import java.util.List;
import java.util.UUID;

public record OrganizationCommercialSubscriptionResponse(
        UUID organizationId,
        String planCode,
        List<String> addOnCodes,
        List<String> serviceCodes,
        List<CommercialServiceQuotaResponse> serviceQuotas,
        List<OrganizationServiceDependencyIssueResponse> dependencyIssues,
        OrganizationServicesResponse entitlements) {

    static OrganizationCommercialSubscriptionResponse from(OrganizationCommercialSubscription subscription) {
        return new OrganizationCommercialSubscriptionResponse(
                subscription.organizationId(),
                subscription.planCode(),
                subscription.addOnCodes(),
                subscription.serviceCodes(),
                subscription.serviceQuotas().stream().map(CommercialServiceQuotaResponse::from).toList(),
                subscription.dependencyIssues().stream().map(OrganizationServiceDependencyIssueResponse::from).toList(),
                OrganizationServicesResponse.from(subscription.entitlements()));
    }
}
