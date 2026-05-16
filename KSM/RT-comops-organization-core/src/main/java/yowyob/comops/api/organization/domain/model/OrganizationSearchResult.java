package yowyob.comops.api.organization.domain.model;

import java.util.UUID;

public record OrganizationSearchResult(
        UUID id,
        UUID tenantId,
        UUID businessActorId,
        String code,
        String service,
        String shortName,
        String longName,
        String legalForm,
        boolean isActive,
        String status) {

    public String legalName() {
        return longName;
    }

    public String displayName() {
        return shortName;
    }

    public String organizationType() {
        return service;
    }
}
