package yowyob.comops.api.treasury.adapter.out.persistence;

import java.math.BigDecimal;
import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "treasury", name = "check_payment")
public record CheckPaymentEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID bankAccountId,
        String checkNumber,
        BigDecimal amount,
        String beneficiary,
        String status) implements PersistableEntity {
}
