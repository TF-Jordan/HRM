package yowyob.comops.api.organization.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record CreateOrganizationRequest(
        @NotNull UUID businessActorId,
        @NotBlank String code,
        @JsonAlias("organizationType") @NotBlank String service,
        Boolean isIndividualBusiness,
        @Email String email,
        @JsonAlias("displayName") @NotBlank String shortName,
        @JsonAlias("legalName") @NotBlank String longName,
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
        Boolean isActive,
        String status) {

    public boolean resolvedIndividualBusiness() {
        return Boolean.TRUE.equals(isIndividualBusiness);
    }

    public boolean resolvedActive() {
        return isActive == null || isActive;
    }
}
