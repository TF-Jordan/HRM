package yowyob.comops.api.common.domain.model;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public enum PlatformServiceCode {
    ORGANIZATION("Organization", "Core organization, agencies, employees and warehouses.", true, false,
            Set.of("ORGANIZATION", "ORGANIZATIONS", "ORG")),
    SETTINGS("Settings", "Core business settings and document sequences.", true, false,
            Set.of("SETTINGS", "SETTING", "CONFIG")),
    COMMERCIAL("Commercial", "Clients, suppliers, prospects, sales agents and third parties.", false, true,
            Set.of("COMMERCIAL", "THIRD_PARTY", "THIRD_PARTIES", "TIERS", "TIER")),
    PRODUCT("Product", "Product catalog management.", false, true,
            Set.of("PRODUCT", "PRODUCTS", "CATALOG")),
    INVENTORY("Inventory", "Stock movements, sessions, transfers and transformations.", false, true,
            Set.of("INVENTORY", "STOCK", "INVENTAIRE")),
    SALES("Sales", "Sales orders and confirmations.", false, true,
            Set.of("SALES", "SALE", "VENTE", "VENTES")),
    BILLING("Billing", "Commercial billing documents and payment workflows.", false, true,
            Set.of("BILLING", "FACTURATION")),
    ACCOUNTING("Accounting", "Accounting, taxes, journals, reports and bookkeeping.", false, true,
            Set.of("ACCOUNTING", "COMPTABILITE", "COMPTA")),
    BANKING("Banking", "Bank accounts, statements, banking transactions, checks and reconciliation workflows.", false, true,
            Set.of("BANKING", "BANQUE", "BANK")),
    TREASURY("Treasury", "Treasury settlements, treasury bank accounts and settlement reconciliations.", false, true,
            Set.of("TREASURY", "TRESORERIE")),
    CASHIER("Cashier", "Cash registers, cashier sessions, cash movements and cashier operations.", false, true,
            Set.of("CASHIER", "CAISSE", "CASH")),
    RESOURCE("Resource", "Material resource lifecycle, reservations and assignments.", false, true,
            Set.of("RESOURCE", "RESOURCES", "RESSOURCE", "RESSOURCES"));

    private final String displayName;
    private final String description;
    private final boolean mandatory;
    private final boolean subscribable;
    private final Set<String> aliases;

    PlatformServiceCode(String displayName, String description, boolean mandatory, boolean subscribable,
            Set<String> aliases) {
        this.displayName = displayName;
        this.description = description;
        this.mandatory = mandatory;
        this.subscribable = subscribable;
        this.aliases = aliases;
    }

    public String code() {
        return name();
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public boolean mandatory() {
        return mandatory;
    }

    public boolean subscribable() {
        return subscribable;
    }

    public static PlatformServiceCode from(String rawCode) {
        String normalized = normalize(rawCode);
        return Arrays.stream(values())
                .filter(service -> service.code().equals(normalized) || service.aliases.contains(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown platform service: " + rawCode));
    }

    public static List<PlatformServiceCode> catalog() {
        return List.of(values());
    }

    public static List<String> mandatoryCodes() {
        return catalog().stream()
                .filter(PlatformServiceCode::mandatory)
                .map(PlatformServiceCode::code)
                .toList();
    }

    public static List<String> subscribableCodes() {
        return catalog().stream()
                .filter(PlatformServiceCode::subscribable)
                .map(PlatformServiceCode::code)
                .toList();
    }

    public static List<String> orderCodes(Set<String> codes) {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        catalog().stream()
                .map(PlatformServiceCode::code)
                .filter(codes::contains)
                .forEach(ordered::add);
        return List.copyOf(ordered);
    }

    private static String normalize(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new IllegalArgumentException("serviceCode is required");
        }
        return rawCode.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
    }
}
