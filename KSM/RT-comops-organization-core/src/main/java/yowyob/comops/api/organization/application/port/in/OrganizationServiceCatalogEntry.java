package yowyob.comops.api.organization.application.port.in;

public record OrganizationServiceCatalogEntry(
        String code,
        String name,
        String description,
        boolean mandatory,
        boolean subscribable) {
}
