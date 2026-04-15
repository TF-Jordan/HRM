package yowyob.comops.api.tp.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record UpdateThirdPartyCommand(
        UUID tenantId,
        UUID thirdPartyId,
        String code,
        String name,
        Set<String> roles,
        boolean prospect,
        String accountingAccount,
        String segment,
        Integer qualificationScore,
        boolean enabled,
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

    public boolean active() {
        return enabled;
    }
}
