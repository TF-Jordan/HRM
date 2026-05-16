package yowyob.comops.api.accounting.extension.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_stock_movement_posting")
public record AccountingExtensionStockMovementPostingEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("movement_reference")
        String movementReference,
        @Column("movement_type")
        String movementType,
        @Column("valuation_amount")
        BigDecimal valuationAmount,
        @Column("currency")
        String currency,
        @Column("created_at")
        Instant createdAt) {
}
