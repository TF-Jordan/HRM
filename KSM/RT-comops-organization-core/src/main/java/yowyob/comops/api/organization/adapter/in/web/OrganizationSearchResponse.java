package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.domain.model.OrganizationSearchResult;
import java.util.UUID;

public record OrganizationSearchResponse(
        UUID id,
        UUID tenantId,
        UUID businessActorId,
        String code,
        String service,
        String shortName,
        String longName,
        String legalForm,
        boolean isActive,
        String status,
        String legalName,
        String displayName,
        String organizationType) {

    public static OrganizationSearchResponse from(OrganizationSearchResult result) {
        return new OrganizationSearchResponse(
                result.id(),
                result.tenantId(),
                result.businessActorId(),
                result.code(),
                result.service(),
                result.shortName(),
                result.longName(),
                result.legalForm(),
                result.isActive(),
                result.status(),
                result.legalName(),
                result.displayName(),
                result.organizationType());
    }
}
