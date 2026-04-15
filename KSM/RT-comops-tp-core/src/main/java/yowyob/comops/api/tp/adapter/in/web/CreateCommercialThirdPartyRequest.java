package yowyob.comops.api.tp.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonAlias;
import yowyob.comops.api.common.domain.model.PartyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateCommercialThirdPartyRequest(
        @NotNull UUID organizationId,
        @NotNull PartyType partyType,
        @NotNull UUID partyId,
        @NotBlank @JsonAlias("referenceCode") String code,
        @NotBlank @JsonAlias("displayName") String name,
        String accountingAccount,
        String segment,
        Integer qualificationScore,
        @JsonAlias("active") Boolean enabled,
        boolean prospect,
        // canonical optional fields
        String type,
        String legalForm,
        String uniqueIdentificationNumber,
        String tradeRegistrationNumber,
        String acronym,
        String longName,
        String logoUri,
        UUID logoId,
        List<String> accountingAccountNumbers,
        List<String> authorizedPaymentMethods,
        BigDecimal authorizedCreditLimit,
        BigDecimal maxDiscountRate,
        Boolean vatSubject,
        BigDecimal operationsBalance,
        BigDecimal openingBalance,
        Integer payTermNumber,
        String payTermType,
        String thirdPartyFamily,
        String classification,
        String taxNumber) {

    public String referenceCode() {
        return code;
    }

    public String displayName() {
        return name;
    }

    public Boolean active() {
        return enabled;
    }
}
