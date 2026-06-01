package yowyob.comops.api.tp.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "tp", name = "third_party")
public record ThirdPartyEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        String partyType,
        UUID partyId,
        String code,
        String referenceCode,
        String displayName,
        Set<String> roles,
        boolean prospect,
        String accountingAccount,
        String segment,
        Integer qualificationScore,
        Instant lastContactedAt,
        Instant nextFollowUpAt,
        String followUpStatus,
        boolean active,
        Instant convertedAt,
        // canonical fields
        String type,
        String legalForm,
        String uniqueIdentificationNumber,
        String tradeRegistrationNumber,
        String name,
        String acronym,
        String longName,
        String logoUri,
        UUID logoId,
        List<String> accountingAccountNumbers,
        List<String> authorizedPaymentMethods,
        BigDecimal authorizedCreditLimit,
        BigDecimal maxDiscountRate,
        boolean vatSubject,
        BigDecimal operationsBalance,
        BigDecimal openingBalance,
        Integer payTermNumber,
        String payTermType,
        String thirdPartyFamily,
        String classification,
        String taxNumber,
        int loyaltyPoints,
        int loyaltyPointsUsed,
        int loyaltyPointsExpired,
        boolean enabled,
        Instant deletedAt) implements PersistableEntity {
}
