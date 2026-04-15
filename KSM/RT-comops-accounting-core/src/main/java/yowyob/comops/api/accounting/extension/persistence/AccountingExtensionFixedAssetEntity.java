package yowyob.comops.api.accounting.extension.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_fixed_asset")
public record AccountingExtensionFixedAssetEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("reference")
        String reference,
        @Column("label")
        String label,
        @Column("acquisition_cost")
        BigDecimal acquisitionCost,
        @Column("useful_life_months")
        int usefulLifeMonths,
        @Column("accumulated_depreciation")
        BigDecimal accumulatedDepreciation,
        @Column("status")
        String status,
        @Column("acquired_at")
        Instant acquiredAt,
        @Column("last_depreciated_at")
        Instant lastDepreciatedAt) {
}
