package yowyob.comops.api.product.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "product", name = "variant")
public record VariantEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID productId,
        String sku,
        String barcode,
        String label,
        boolean isDefault,
        String status) implements PersistableEntity {
}
