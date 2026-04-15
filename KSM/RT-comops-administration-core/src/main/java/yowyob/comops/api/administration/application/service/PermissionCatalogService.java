package yowyob.comops.api.administration.application.service;

import yowyob.comops.api.administration.domain.model.PermissionCatalogEntry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PermissionCatalogService {

    private final Map<String, PermissionCatalogEntry> catalogByCode;
    private final Set<String> protectedPermissions = Set.of("system:admin", "iam:admin", "tenant:admin",
            "management:read", "system:observe");

    public PermissionCatalogService() {
        List<PermissionCatalogEntry> entries = List.of(
                entry("administration:read", "Administration Read", "Read administration data.", "ADMINISTRATION", "AGENCY", false, true),
                entry("administration:write", "Administration Write", "Manage administration resources.", "ADMINISTRATION", "TENANT", false, true),
                entry("administration:roles:read", "Administration Roles Read", "Read administrative roles.", "ADMINISTRATION", "AGENCY", false, true),
                entry("administration:roles:write", "Administration Roles Write", "Create, update and delete administrative roles.", "ADMINISTRATION", "TENANT", false, true),
                entry("administration:roles:clone", "Administration Roles Clone", "Clone administrative roles from existing templates or custom roles.", "ADMINISTRATION", "TENANT", false, true),
                entry("administration:permissions:read", "Permissions Catalog Read", "Read the permission catalog.", "ADMINISTRATION", "TENANT", false, true),
                entry("administration:assignments:write", "Role Assignment Write", "Assign and revoke roles.", "ADMINISTRATION", "AGENCY", false, true),
                entry("administration:settings:read", "Administration Settings Read", "Read administrative platform settings.", "ADMINISTRATION", "TENANT", false, true),
                entry("administration:settings:write", "Administration Settings Write", "Manage administrative platform settings.", "ADMINISTRATION", "TENANT", false, true),
                entry("administration:audit:read", "Administration Audit Read", "Read administration audit entries.", "ADMINISTRATION", "TENANT", false, true),
                entry("administration:govern:business-actors", "Business Actors Governance", "Approve, reject, suspend, block and reactivate business actors.", "ADMINISTRATION", "TENANT", false, true),
                entry("administration:govern:organizations", "Organizations Governance", "Approve, reject, suspend, close and reopen organizations.", "ADMINISTRATION", "TENANT", false, true),
                entry("administration:govern:agencies", "Agencies Governance", "Activate, suspend and close agencies.", "ADMINISTRATION", "ORGANIZATION", false, true),
                entry("organizations:write", "Organizations Write", "Manage organizations, agencies, warehouses and employees.", "ORGANIZATION", "ORGANIZATION", false, true),
                entry("third-parties:write", "Third Parties Write", "Manage clients, suppliers and prospects.", "COMMERCIAL", "ORGANIZATION", false, true),
                entry("products:write", "Products Write", "Manage product catalog entries.", "PRODUCT", "AGENCY", false, true),
                entry("inventory:write", "Inventory Write", "Manage stock movements, sessions and transfers.", "INVENTORY", "AGENCY", false, true),
                entry("sales:write", "Sales Write", "Manage sales orders.", "SALES", "AGENCY", false, true),
                entry("accounting:read", "Accounting Read", "Read invoices and accounting information.", "ACCOUNTING", "ORGANIZATION", false, true),
                entry("accounting:write", "Accounting Write", "Manage invoices and accounting documents.", "ACCOUNTING", "ORGANIZATION", false, true),
                entry("accounting:post", "Accounting Post", "Post invoices and accounting entries.", "ACCOUNTING", "ORGANIZATION", false, true),
                entry("treasury:manage", "Treasury Manage", "Manage bank accounts, checks and reconciliations.", "TREASURY", "ORGANIZATION", false, true),
                entry("treasury:read", "Treasury Read", "Read treasury information.", "TREASURY", "AGENCY", false, true),
                entry("treasury:settle-invoices", "Treasury Invoice Settlement", "Register invoice settlements.", "TREASURY", "ORGANIZATION", false, true),
                entry("resources:write", "Resources Write", "Manage material resources.", "RESOURCE", "AGENCY", false, true),
                entry("resources:reserve", "Resources Reserve", "Reserve resources.", "RESOURCE", "AGENCY", false, true),
                entry("resources:unassign", "Resources Unassign", "Unassign resources.", "RESOURCE", "AGENCY", false, true),
                entry("resources:dispose", "Resources Dispose", "Dispose resources.", "RESOURCE", "AGENCY", false, true),
                entry("settings:write", "Settings Write", "Manage business settings and document sequences.", "SETTINGS", "ORGANIZATION", false, true),
                entry("system:observe", "System Observe", "Read operational observability data.", "SYSTEM", "SYSTEM", true, false),
                entry("system:admin", "System Admin", "Full system administration.", "SYSTEM", "SYSTEM", true, false),
                entry("iam:admin", "IAM Admin", "Manage identity and access management globally.", "IAM", "SYSTEM", true, false),
                entry("tenant:admin", "Tenant Admin", "Administer a full tenant.", "ADMINISTRATION", "TENANT", true, false),
                entry("management:read", "Management Read", "Read technical management endpoints.", "SYSTEM", "SYSTEM", true, false));
        Map<String, PermissionCatalogEntry> map = new LinkedHashMap<>();
        entries.forEach(entry -> map.put(entry.code().toLowerCase(Locale.ROOT), entry));
        this.catalogByCode = Map.copyOf(map);
    }

    public List<PermissionCatalogEntry> list() {
        return catalogByCode.values().stream().toList();
    }

    public PermissionCatalogEntry get(String code) {
        return catalogByCode.get(normalize(code));
    }

    public boolean isKnown(String code) {
        return catalogByCode.containsKey(normalize(code));
    }

    public boolean isProtected(String code) {
        return protectedPermissions.contains(normalize(code));
    }

    private PermissionCatalogEntry entry(String code, String name, String description, String module, String scope,
            boolean system, boolean assignable) {
        return new PermissionCatalogEntry(code, name, description, module, scope, system, assignable, false);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
