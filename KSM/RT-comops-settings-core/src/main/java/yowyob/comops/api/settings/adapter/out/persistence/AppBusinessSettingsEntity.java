package yowyob.comops.api.settings.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "settings", name = "app_business_settings")
public record AppBusinessSettingsEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        boolean negotiateSellingPrice,
        boolean sellingPriceIncludeVat,
        boolean authorizeExceptionalDiscount,
        double grantableDiscountRate,
        boolean printLogo,
        String paperFormat,
        int lengthOfVatInvoiceNumber,
        String prefixOfVatInvoiceNumber,
        boolean lowStockAlert,
        boolean preventiveMaintenanceAlert,
        String defaultCurrency,
        String legalIdentity,
        String taxIdentifier,
        boolean requireSalesOrderApproval,
        boolean requireReturnApproval) implements PersistableEntity {
}
