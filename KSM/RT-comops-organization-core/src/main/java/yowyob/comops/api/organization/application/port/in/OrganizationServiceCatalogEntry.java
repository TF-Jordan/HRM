package yowyob.comops.api.organization.application.port.in;

import java.util.List;

public record OrganizationServiceCatalogEntry(
        String code,
        String name,
        String description,
        boolean mandatory,
        boolean subscribable,
        List<String> requiredDependencies,
        List<String> recommendedDependencies,
        List<String> packs) {
}
