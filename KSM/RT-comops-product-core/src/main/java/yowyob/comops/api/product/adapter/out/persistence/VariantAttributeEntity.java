package yowyob.comops.api.product.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "product", name = "variant_attribute")
public record VariantAttributeEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID variantId,
        String attributeName,
        String attributeValue) implements PersistableEntity {
}
