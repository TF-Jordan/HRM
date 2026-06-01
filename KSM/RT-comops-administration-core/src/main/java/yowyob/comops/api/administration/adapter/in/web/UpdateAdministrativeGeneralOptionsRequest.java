package yowyob.comops.api.administration.adapter.in.web;

import java.util.UUID;

public record UpdateAdministrativeGeneralOptionsRequest(UUID agencyId, boolean negotiateSellingPrice,
        boolean sellingPriceIncludeVat, boolean authorizeExceptionalDiscount, double grantableDiscountRate,
        boolean printLogo, String paperFormat, int lengthOfVatInvoiceNumber, String prefixOfVatInvoiceNumber,
        boolean lowStockAlert, boolean preventiveMaintenanceAlert, String defaultCurrency, String legalIdentity,
        String taxIdentifier, boolean requireSalesOrderApproval, boolean requireReturnApproval) {
}
