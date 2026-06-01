package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.application.port.in.OrganizationServiceDependencyIssue;
import java.util.List;

public record OrganizationServiceDependencyIssueResponse(
        String serviceCode,
        List<String> missingRequiredServices,
        List<String> missingRecommendedServices) {

    public static OrganizationServiceDependencyIssueResponse from(OrganizationServiceDependencyIssue issue) {
        return new OrganizationServiceDependencyIssueResponse(issue.serviceCode(), issue.missingRequiredServices(),
                issue.missingRecommendedServices());
    }
}
