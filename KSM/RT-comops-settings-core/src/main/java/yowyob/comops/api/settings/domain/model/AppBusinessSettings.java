package yowyob.comops.api.settings.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class AppBusinessSettings extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final boolean negotiateSellingPrice;
    private final boolean sellingPriceIncludeVat;
    private final boolean authorizeExceptionalDiscount;
    private final double grantableDiscountRate;
    private final boolean printLogo;
    private final String paperFormat;
    private final int lengthOfVatInvoiceNumber;
    private final String prefixOfVatInvoiceNumber;
    private final boolean lowStockAlert;
    private final boolean preventiveMaintenanceAlert;
    private final String defaultCurrency;
    private final String legalIdentity;
    private final String taxIdentifier;
    private final boolean requireSalesOrderApproval;
    private final boolean requireReturnApproval;

    private AppBusinessSettings(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId, UUID agencyId,
            boolean negotiateSellingPrice, boolean sellingPriceIncludeVat, boolean authorizeExceptionalDiscount,
            double grantableDiscountRate, boolean printLogo, String paperFormat, int lengthOfVatInvoiceNumber,
            String prefixOfVatInvoiceNumber, boolean lowStockAlert, boolean preventiveMaintenanceAlert,
            String defaultCurrency, String legalIdentity, String taxIdentifier, boolean requireSalesOrderApproval,
            boolean requireReturnApproval) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = agencyId;
        this.negotiateSellingPrice = negotiateSellingPrice;
        this.sellingPriceIncludeVat = sellingPriceIncludeVat;
        this.authorizeExceptionalDiscount = authorizeExceptionalDiscount;
        this.grantableDiscountRate = normalizeDiscountRate(grantableDiscountRate);
        this.printLogo = printLogo;
        this.paperFormat = normalizePaperFormat(paperFormat);
        this.lengthOfVatInvoiceNumber = normalizeInvoiceNumberLength(lengthOfVatInvoiceNumber);
        this.prefixOfVatInvoiceNumber = normalizePrefix(prefixOfVatInvoiceNumber);
        this.lowStockAlert = lowStockAlert;
        this.preventiveMaintenanceAlert = preventiveMaintenanceAlert;
        this.defaultCurrency = normalizeCurrency(defaultCurrency);
        this.legalIdentity = normalizeOptionalText(legalIdentity);
        this.taxIdentifier = normalizeOptionalText(taxIdentifier);
        this.requireSalesOrderApproval = requireSalesOrderApproval;
        this.requireReturnApproval = requireReturnApproval;
    }

    public static AppBusinessSettings defaults(UUID tenantId, UUID organizationId) {
        return defaults(tenantId, organizationId, null);
    }

    public static AppBusinessSettings defaults(UUID tenantId, UUID organizationId, UUID agencyId) {
        Instant now = Instant.now();
        return new AppBusinessSettings(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                false, false, false, 0.0d, true, "A4", 10, "FAC-", false, false,
                "XAF", null, null, false, false);
    }

    public static AppBusinessSettings rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, boolean negotiateSellingPrice, boolean sellingPriceIncludeVat,
            boolean authorizeExceptionalDiscount, double grantableDiscountRate, boolean printLogo, String paperFormat,
            int lengthOfVatInvoiceNumber, String prefixOfVatInvoiceNumber, boolean lowStockAlert,
            boolean preventiveMaintenanceAlert, String defaultCurrency, String legalIdentity, String taxIdentifier,
            boolean requireSalesOrderApproval, boolean requireReturnApproval) {
        return new AppBusinessSettings(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                negotiateSellingPrice, sellingPriceIncludeVat, authorizeExceptionalDiscount, grantableDiscountRate,
                printLogo, paperFormat, lengthOfVatInvoiceNumber, prefixOfVatInvoiceNumber, lowStockAlert,
                preventiveMaintenanceAlert, defaultCurrency, legalIdentity, taxIdentifier,
                requireSalesOrderApproval, requireReturnApproval);
    }

    public AppBusinessSettings update(UUID agencyId, boolean negotiateSellingPrice, boolean sellingPriceIncludeVat,
            boolean authorizeExceptionalDiscount, double grantableDiscountRate, boolean printLogo, String paperFormat,
            int lengthOfVatInvoiceNumber, String prefixOfVatInvoiceNumber, boolean lowStockAlert,
            boolean preventiveMaintenanceAlert, String defaultCurrency, String legalIdentity, String taxIdentifier,
            boolean requireSalesOrderApproval, boolean requireReturnApproval) {
        return new AppBusinessSettings(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                negotiateSellingPrice, sellingPriceIncludeVat, authorizeExceptionalDiscount, grantableDiscountRate,
                printLogo, paperFormat, lengthOfVatInvoiceNumber, prefixOfVatInvoiceNumber, lowStockAlert,
                preventiveMaintenanceAlert, defaultCurrency, legalIdentity, taxIdentifier,
                requireSalesOrderApproval, requireReturnApproval);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public boolean negotiateSellingPrice() { return negotiateSellingPrice; }
    public boolean sellingPriceIncludeVat() { return sellingPriceIncludeVat; }
    public boolean authorizeExceptionalDiscount() { return authorizeExceptionalDiscount; }
    public double grantableDiscountRate() { return grantableDiscountRate; }
    public boolean printLogo() { return printLogo; }
    public String paperFormat() { return paperFormat; }
    public int lengthOfVatInvoiceNumber() { return lengthOfVatInvoiceNumber; }
    public String prefixOfVatInvoiceNumber() { return prefixOfVatInvoiceNumber; }
    public boolean lowStockAlert() { return lowStockAlert; }
    public boolean preventiveMaintenanceAlert() { return preventiveMaintenanceAlert; }
    public String defaultCurrency() { return defaultCurrency; }
    public String legalIdentity() { return legalIdentity; }
    public String taxIdentifier() { return taxIdentifier; }
    public boolean requireSalesOrderApproval() { return requireSalesOrderApproval; }
    public boolean requireReturnApproval() { return requireReturnApproval; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static double normalizeDiscountRate(double value) {
        if (value < 0.0d || value > 100.0d) {
            throw new IllegalArgumentException("grantableDiscountRate must be between 0 and 100");
        }
        return value;
    }

    private static String normalizePaperFormat(String value) {
        if (value == null || value.isBlank()) {
            return "A4";
        }
        return value.trim().toUpperCase();
    }

    private static String normalizeCurrency(String value) {
        if (value == null || value.isBlank()) {
            return "XAF";
        }
        return value.trim().toUpperCase();
    }

    private static int normalizeInvoiceNumberLength(int value) {
        if (value < 1 || value > 32) {
            throw new IllegalArgumentException("lengthOfVatInvoiceNumber must be between 1 and 32");
        }
        return value;
    }

    private static String normalizePrefix(String value) {
        if (value == null || value.isBlank()) {
            return "FAC-";
        }
        return value.trim().toUpperCase();
    }

    private static String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
