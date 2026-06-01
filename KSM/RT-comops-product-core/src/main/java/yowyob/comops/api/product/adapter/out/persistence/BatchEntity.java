package yowyob.comops.api.product.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "product", name = "batch")
public record BatchEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID productId,
        String lotNumber,
        LocalDate manufacturingDate,
        LocalDate expiryDate,
        int quantity) implements PersistableEntity {
}
