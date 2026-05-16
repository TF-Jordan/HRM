package yowyob.comops.api.settings.adapter.in.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpdateAppBusinessSettingsRequest(
        UUID agencyId,
        boolean negotiateSellingPrice,
        boolean sellingPriceIncludeVat,
        boolean authorizeExceptionalDiscount,
        @Min(0) @Max(100) double grantableDiscountRate,
        boolean printLogo,
        @NotBlank String paperFormat,
        @Min(1) @Max(32) int lengthOfVatInvoiceNumber,
        @NotBlank String prefixOfVatInvoiceNumber,
        boolean lowStockAlert,
        boolean preventiveMaintenanceAlert,
        String defaultCurrency,
        String legalIdentity,
        String taxIdentifier,
        boolean requireSalesOrderApproval,
        boolean requireReturnApproval) {
}
