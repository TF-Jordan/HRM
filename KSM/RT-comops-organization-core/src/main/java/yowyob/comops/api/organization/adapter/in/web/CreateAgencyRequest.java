package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record CreateAgencyRequest(
        @NotBlank String code,
        UUID ownerId,
        UUID managerId,
        @NotBlank String name,
        String location,
        String description,
        Boolean transferable,
        Boolean active,
        String logoUri,
        UUID logoId,
        String shortName,
        String longName,
        Boolean isIndividualBusiness,
        Boolean isHeadquarter,
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
        Boolean isPublic,
        Boolean isBusiness,
        Integer totalAffiliatedCustomers,
        String agencyType) {

    public boolean resolvedTransferable() {
        return Boolean.TRUE.equals(transferable);
    }

    public boolean resolvedActive() {
        return active == null || active;
    }

    public boolean resolvedIndividualBusiness() {
        return Boolean.TRUE.equals(isIndividualBusiness);
    }

    public boolean resolvedHeadquarter() {
        return Boolean.TRUE.equals(isHeadquarter);
    }

    public boolean resolvedPublic() {
        return Boolean.TRUE.equals(isPublic);
    }

    public boolean resolvedBusiness() {
        return Boolean.TRUE.equals(isBusiness);
    }
}
