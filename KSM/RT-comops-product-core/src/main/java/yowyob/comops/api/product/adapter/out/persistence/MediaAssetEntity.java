package yowyob.comops.api.product.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "product", name = "media_asset")
public record MediaAssetEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        String targetType,
        UUID targetId,
        UUID fileId,
        String mimeType,
        int position,
        String altText) implements PersistableEntity {
}
