package yowyob.comops.api.accounting.extension.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_item")
public record AccountingExtensionItemEntity(
        @Id
        @Column("item_key")
        String itemKey,
        @Column("scope")
        String scope,
        @Column("item_type")
        String itemType,
        @Column("item_id")
        UUID itemId,
        @Column("organization_id")
        UUID organizationId,
        @Column("payload")
        String payload,
        @Column("updated_at")
        Instant updatedAt) {
}
