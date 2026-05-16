package yowyob.comops.api.organization.application.port.in;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record UpdateAgencyCommand(
        UUID tenantId,
        UUID agencyId,
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
        String agencyType) {
}
