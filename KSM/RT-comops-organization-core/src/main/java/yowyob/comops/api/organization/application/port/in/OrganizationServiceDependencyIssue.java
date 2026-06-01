package yowyob.comops.api.organization.application.port.in;

import java.util.List;

public record OrganizationServiceDependencyIssue(
        String serviceCode,
        List<String> missingRequiredServices,
        List<String> missingRecommendedServices) {
}
