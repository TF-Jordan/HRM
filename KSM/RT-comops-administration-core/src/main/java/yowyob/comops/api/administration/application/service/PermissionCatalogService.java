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
                entry("cashier:write", "Cashier Write", "Manage cashier settlements and point-of-sale payments.", "CASHIER", "AGENCY", false, true),
                entry("settings:read", "Settings Read", "Read business settings and operational policies.", "SETTINGS", "ORGANIZATION", false, true),
                entry("settings:write", "Settings Write", "Manage business settings and document sequences.", "SETTINGS", "ORGANIZATION", false, true),
                entry("hrm:employee:create", "HRM Employee Create", "Create employee records.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:employee:read", "HRM Employee Read", "Read employee records.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:employee:update", "HRM Employee Update", "Update employee records.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:employee:terminate", "HRM Employee Terminate", "Terminate employees.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:employee:suspend", "HRM Employee Suspend", "Suspend employees.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:employee:reactivate", "HRM Employee Reactivate", "Reactivate suspended employees.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:contract:create", "HRM Contract Create", "Create employee contracts.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:contract:read", "HRM Contract Read", "Read employee contracts.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:dependent:create", "HRM Dependent Create", "Create employee dependents.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:dependent:read", "HRM Dependent Read", "Read employee dependents.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:onboarding:create", "HRM Onboarding Create", "Create onboarding workflows.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:onboarding:read", "HRM Onboarding Read", "Read onboarding workflows.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:onboarding:manage", "HRM Onboarding Manage", "Manage onboarding workflow steps.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:recruitment:create", "HRM Recruitment Create", "Create recruitment records.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:recruitment:read", "HRM Recruitment Read", "Read recruitment records.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:recruitment:manage", "HRM Recruitment Manage", "Manage recruitment workflows.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:payroll:run", "HRM Payroll Run", "Run payroll cycles.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:payroll:validate", "HRM Payroll Validate", "Validate payroll cycles.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:payroll:read", "HRM Payroll Read", "Read payroll cycles and payslips.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:leave:create", "HRM Leave Create", "Create leave requests.", "HRM", "AGENCY", false, true),
                entry("hrm:leave:approve", "HRM Leave Approve", "Approve or reject leave requests.", "HRM", "AGENCY", false, true),
                entry("hrm:leave:read", "HRM Leave Read", "Read leave requests and balances.", "HRM", "AGENCY", false, true),
                entry("hrm:timesheet:create", "HRM Timesheet Create", "Create timesheet records.", "HRM", "AGENCY", false, true),
                entry("hrm:timesheet:validate", "HRM Timesheet Validate", "Validate timesheet records.", "HRM", "AGENCY", false, true),
                entry("hrm:timesheet:read", "HRM Timesheet Read", "Read timesheet records.", "HRM", "AGENCY", false, true),
                entry("hrm:expense:create", "HRM Expense Create", "Create employee expense claims.", "HRM", "AGENCY", false, true),
                entry("hrm:expense:manage", "HRM Expense Manage", "Approve, reject and reimburse employee expense claims.", "HRM", "AGENCY", false, true),
                entry("hrm:expense:read", "HRM Expense Read", "Read employee expense claims.", "HRM", "AGENCY", false, true),
                entry("hrm:loan:create", "HRM Loan Create", "Create employee loan or advance requests.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:loan:approve", "HRM Loan Approve", "Approve employee loan or advance requests.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:loan:read", "HRM Loan Read", "Read employee loan or advance requests.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:mission:create", "HRM Mission Create", "Create employee missions.", "HRM", "AGENCY", false, true),
                entry("hrm:mission:manage", "HRM Mission Manage", "Manage employee mission lifecycle.", "HRM", "AGENCY", false, true),
                entry("hrm:mission:read", "HRM Mission Read", "Read employee missions.", "HRM", "AGENCY", false, true),
                entry("hrm:medical:create", "HRM Medical Create", "Create employee medical records.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:medical:read", "HRM Medical Read", "Read employee medical records.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:review:create", "HRM Review Create", "Create performance reviews.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:review:manage", "HRM Review Manage", "Manage performance review lifecycle.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:review:read", "HRM Review Read", "Read performance reviews.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:training:create", "HRM Training Create", "Create training programs and sessions.", "HRM", "AGENCY", false, true),
                entry("hrm:training:manage", "HRM Training Manage", "Manage training program lifecycle.", "HRM", "AGENCY", false, true),
                entry("hrm:training:read", "HRM Training Read", "Read training programs and sessions.", "HRM", "AGENCY", false, true),
                entry("hrm:training:request", "HRM Training Request", "Submit a self-service training request for approval.", "HRM", "AGENCY", false, true),
                entry("hrm:budget:create", "HRM Training Budget Create", "Create training budgets.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:budget:manage", "HRM Training Budget Manage", "Manage training budget lifecycle.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:budget:read", "HRM Training Budget Read", "Read training budgets.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:skill:create", "HRM Skill Create", "Create employee skills and endorsements.", "HRM", "AGENCY", false, true),
                entry("hrm:skill:read", "HRM Skill Read", "Read employee skills and endorsements.", "HRM", "AGENCY", false, true),
                entry("hrm:declaration:create", "HRM Declaration Create", "Create social declarations.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:declaration:manage", "HRM Declaration Manage", "Manage social declaration lifecycle.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:declaration:read", "HRM Declaration Read", "Read social declarations.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:kpi:create", "HRM KPI Create", "Create HR KPI snapshots.", "HRM", "ORGANIZATION", false, true),
                entry("hrm:kpi:read", "HRM KPI Read", "Read HR KPI snapshots.", "HRM", "ORGANIZATION", false, true),
                entry("blockchain:wallet:create", "Blockchain Wallet Create", "Create blockchain wallets for organizations.", "BLOCKCHAIN", "ORGANIZATION", false, true),
                entry("blockchain:wallet:read", "Blockchain Wallet Read", "Read blockchain wallets.", "BLOCKCHAIN", "ORGANIZATION", false, true),
                entry("blockchain:transaction:sign", "Blockchain Transaction Sign", "Build and sign blockchain transaction payloads.", "BLOCKCHAIN", "ORGANIZATION", false, true),
                entry("blockchain:transaction:create", "Blockchain Transaction Create", "Submit blockchain transactions.", "BLOCKCHAIN", "ORGANIZATION", false, true),
                entry("blockchain:transaction:read", "Blockchain Transaction Read", "Read blockchain transactions.", "BLOCKCHAIN", "ORGANIZATION", false, true),
                entry("blockchain:anchor:create", "Blockchain Anchor Create", "Anchor business documents on the blockchain.", "BLOCKCHAIN", "ORGANIZATION", false, true),
                entry("blockchain:block:mine", "Blockchain Block Mine", "Mine pending blockchain blocks.", "BLOCKCHAIN", "ORGANIZATION", false, true),
                entry("blockchain:block:read", "Blockchain Block Read", "Read blockchain blocks.", "BLOCKCHAIN", "ORGANIZATION", false, true),
                entry("blockchain:chain:validate", "Blockchain Chain Validate", "Validate blockchain integrity.", "BLOCKCHAIN", "ORGANIZATION", false, true),
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
