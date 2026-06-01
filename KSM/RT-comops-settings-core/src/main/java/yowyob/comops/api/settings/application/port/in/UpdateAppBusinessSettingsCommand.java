package yowyob.comops.api.settings.application.port.in;

import java.util.UUID;

public record UpdateAppBusinessSettingsCommand(
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
}
