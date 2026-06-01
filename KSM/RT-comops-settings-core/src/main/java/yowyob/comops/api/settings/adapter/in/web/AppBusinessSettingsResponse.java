package yowyob.comops.api.settings.adapter.in.web;

import yowyob.comops.api.settings.domain.model.AppBusinessSettings;
import java.util.UUID;

public record AppBusinessSettingsResponse(
        UUID id,
        UUID tenantId,
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
        boolean requireReturnApproval) {

    public static AppBusinessSettingsResponse from(AppBusinessSettings settings) {
        return new AppBusinessSettingsResponse(settings.id(), settings.tenantId(), settings.organizationId(),
                settings.agencyId(), settings.negotiateSellingPrice(), settings.sellingPriceIncludeVat(),
                settings.authorizeExceptionalDiscount(), settings.grantableDiscountRate(), settings.printLogo(),
                settings.paperFormat(), settings.lengthOfVatInvoiceNumber(), settings.prefixOfVatInvoiceNumber(),
                settings.lowStockAlert(), settings.preventiveMaintenanceAlert(), settings.defaultCurrency(),
                settings.legalIdentity(), settings.taxIdentifier(), settings.requireSalesOrderApproval(),
                settings.requireReturnApproval());
    }
}
