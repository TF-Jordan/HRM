package yowyob.comops.api.tp.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "tp", name = "third_party_bank_account")
public record ThirdPartyBankAccountEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID thirdPartyId,
        String label,
        String bankName,
        String iban,
        String swiftBic,
        String currency,
        boolean primaryAccount) implements PersistableEntity {
}
