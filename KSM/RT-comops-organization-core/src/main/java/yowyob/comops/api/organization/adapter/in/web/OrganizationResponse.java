package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.domain.model.Organization;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        UUID tenantId,
        UUID businessActorId,
        String governanceStatus,
        UUID governedByUserId,
        Instant governedAt,
        String governanceReason,
        String code,
        String service,
        boolean isIndividualBusiness,
        String email,
        String shortName,
        String longName,
        String description,
        String logoUri,
        UUID logoId,
        String websiteUrl,
        String socialNetwork,
        String businessRegistrationNumber,
        String taxNumber,
        BigDecimal capitalShare,
        String ceoName,
        Integer yearFounded,
        Set<String> keywords,
        Integer numberOfEmployees,
        String legalForm,
        boolean isActive,
        String status,
        Instant deletedAt,
        String legalName,
        String displayName,
        String organizationType) {

    public static OrganizationResponse from(Organization organization) {
        return new OrganizationResponse(
                organization.id(),
                organization.tenantId(),
                organization.businessActorId(),
                organization.governanceStatus().name(),
                organization.governedByUserId(),
                organization.governedAt(),
                organization.governanceReason(),
                organization.code(),
                organization.service(),
                organization.isIndividualBusiness(),
                organization.email(),
                organization.shortName(),
                organization.longName(),
                organization.description(),
                organization.logoUri(),
                organization.logoId(),
                organization.websiteUrl(),
                organization.socialNetwork(),
                organization.businessRegistrationNumber(),
                organization.taxNumber(),
                organization.capitalShare(),
                organization.ceoName(),
                organization.yearFounded(),
                organization.keywords(),
                organization.numberOfEmployees(),
                organization.legalForm(),
                organization.isActive(),
                organization.status(),
                organization.deletedAt(),
                organization.legalName(),
                organization.displayName(),
                organization.organizationType());
    }
}
