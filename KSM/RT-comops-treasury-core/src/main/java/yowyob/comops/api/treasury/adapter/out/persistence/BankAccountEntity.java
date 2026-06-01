package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "treasury", name = "bank_account")
public record BankAccountEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID bankThirdPartyId,
        UUID ownerThirdPartyId,
        String bankName,
        String accountNumber,
        String iban,
        String currency,
        String status) implements PersistableEntity {
}
