package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.application.port.in.OrganizationServiceCatalogEntry;

public record OrganizationServiceCatalogResponse(
        String code,
        String name,
        String description,
        boolean mandatory,
        boolean subscribable) {

    public static OrganizationServiceCatalogResponse from(OrganizationServiceCatalogEntry entry) {
        return new OrganizationServiceCatalogResponse(entry.code(), entry.name(), entry.description(), entry.mandatory(),
                entry.subscribable());
    }
}
