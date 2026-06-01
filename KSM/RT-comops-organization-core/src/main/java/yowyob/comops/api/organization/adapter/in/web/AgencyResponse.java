package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.domain.model.Agency;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AgencyResponse(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        String governanceStatus,
        UUID governedByUserId,
        Instant governedAt,
        String governanceReason,
        String code,
        UUID ownerId,
        UUID managerId,
        String name,
        String location,
        String description,
        boolean transferable,
        boolean active,
        String logoUri,
        UUID logoId,
        String shortName,
        String longName,
        boolean isIndividualBusiness,
        boolean isHeadquarter,
        String country,
        String city,
        Double latitude,
        Double longitude,
        String openTime,
        String closeTime,
        String phone,
        String email,
        String whatsapp,
        String greetingMessage,
        BigDecimal averageRevenue,
        BigDecimal capitalShare,
        String registrationNumber,
        String socialNetwork,
        String taxNumber,
        Set<String> keywords,
        boolean isPublic,
        boolean isBusiness,
        Integer totalAffiliatedCustomers,
        Instant deletedAt,
        String agencyType) {

    public static AgencyResponse from(Agency agency) {
        return new AgencyResponse(agency.id(), agency.tenantId(), agency.organizationId(),
                agency.governanceStatus().name(), agency.governedByUserId(), agency.governedAt(),
                agency.governanceReason(), agency.code(), agency.ownerId(), agency.managerId(), agency.name(),
                agency.location(), agency.description(), agency.transferable(), agency.active(), agency.logoUri(),
                agency.logoId(), agency.shortName(), agency.longName(), agency.isIndividualBusiness(),
                agency.isHeadquarter(), agency.country(), agency.city(), agency.latitude(), agency.longitude(),
                agency.openTime(), agency.closeTime(), agency.phone(), agency.email(), agency.whatsapp(),
                agency.greetingMessage(), agency.averageRevenue(), agency.capitalShare(),
                agency.registrationNumber(), agency.socialNetwork(), agency.taxNumber(), agency.keywords(),
                agency.isPublic(), agency.isBusiness(), agency.totalAffiliatedCustomers(), agency.deletedAt(),
                agency.agencyType());
    }
}
