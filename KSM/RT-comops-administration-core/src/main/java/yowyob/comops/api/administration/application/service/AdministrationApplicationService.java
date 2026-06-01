package yowyob.comops.api.administration.application.service;

import yowyob.comops.api.actor.application.port.out.BusinessActorProfileRepository;
import yowyob.comops.api.actor.domain.model.BusinessActorProfile;
import yowyob.comops.api.administration.application.port.in.AssignAdministrativeRoleCommand;
import yowyob.comops.api.administration.application.port.in.AssignAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.CloneAdministrativeRoleCommand;
import yowyob.comops.api.administration.application.port.in.CloneAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.CreateAdministrativeRoleCommand;
import yowyob.comops.api.administration.application.port.in.CreateAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.DeleteAdministrativeRoleCommand;
import yowyob.comops.api.administration.application.port.in.DeleteAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.GetAdministrativePlatformOptionsUseCase;
import yowyob.comops.api.administration.application.port.in.GetAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.GovernAgencyCommand;
import yowyob.comops.api.administration.application.port.in.GovernAgencyUseCase;
import yowyob.comops.api.administration.application.port.in.GovernBusinessActorCommand;
import yowyob.comops.api.administration.application.port.in.GovernBusinessActorUseCase;
import yowyob.comops.api.administration.application.port.in.GovernOrganizationCommand;
import yowyob.comops.api.administration.application.port.in.GovernOrganizationUseCase;
import yowyob.comops.api.administration.application.port.in.ListAdministrationAuditUseCase;
import yowyob.comops.api.administration.application.port.in.ListAdministrativeRoleTemplatesUseCase;
import yowyob.comops.api.administration.application.port.in.ListAdministrativeRolesUseCase;
import yowyob.comops.api.administration.application.port.in.ListBusinessActorGovernanceUseCase;
import yowyob.comops.api.administration.application.port.in.ListGovernedAgenciesUseCase;
import yowyob.comops.api.administration.application.port.in.ListGovernedOrganizationsUseCase;
import yowyob.comops.api.administration.application.port.in.ListPermissionCatalogUseCase;
import yowyob.comops.api.administration.application.port.in.ListUserRoleAssignmentsUseCase;
import yowyob.comops.api.administration.application.port.in.ProvisionAdministrativeRoleTemplatesUseCase;
import yowyob.comops.api.administration.application.port.in.ReplaceRolePermissionsCommand;
import yowyob.comops.api.administration.application.port.in.ReplaceRolePermissionsUseCase;
import yowyob.comops.api.administration.application.port.in.RevokeAdministrativeRoleCommand;
import yowyob.comops.api.administration.application.port.in.RevokeAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.UpdateAdministrativePlatformOptionsCommand;
import yowyob.comops.api.administration.application.port.in.UpdateAdministrativePlatformOptionsUseCase;
import yowyob.comops.api.administration.application.port.in.UpdateAdministrativeRoleCommand;
import yowyob.comops.api.administration.application.port.in.UpdateAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.ListTenantUsersUseCase;
import yowyob.comops.api.administration.application.port.out.AdminAuditRepository;
import yowyob.comops.api.administration.application.port.out.AdministrativePlatformOptionsRepository;
import yowyob.comops.api.administration.domain.PermissionCatalogValidationException;
import yowyob.comops.api.administration.domain.ProtectedRoleMutationException;
import yowyob.comops.api.administration.domain.RoleStillAssignedException;
import yowyob.comops.api.administration.domain.UserRoleAssignmentNotFoundException;
import yowyob.comops.api.administration.domain.model.AdminAuditEntry;
import yowyob.comops.api.administration.domain.model.AdministrativePlatformOptions;
import yowyob.comops.api.administration.domain.model.AdministrativeRoleTemplate;
import yowyob.comops.api.administration.domain.model.PermissionCatalogEntry;
import yowyob.comops.api.auth.application.port.out.UserAccountRepository;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.domain.model.Agency;
import yowyob.comops.api.organization.domain.model.Organization;
import yowyob.comops.api.roles.application.port.out.RoleRepository;
import yowyob.comops.api.roles.application.port.out.UserRoleAssignmentRepository;
import yowyob.comops.api.roles.domain.DuplicateRoleCodeException;
import yowyob.comops.api.roles.domain.model.Role;
import yowyob.comops.api.roles.domain.model.RoleScopeType;
import yowyob.comops.api.roles.domain.model.UserRoleAssignment;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AdministrationApplicationService implements ListPermissionCatalogUseCase, ListAdministrativeRolesUseCase,
        GetAdministrativeRoleUseCase, CreateAdministrativeRoleUseCase, UpdateAdministrativeRoleUseCase,
        ReplaceRolePermissionsUseCase, DeleteAdministrativeRoleUseCase, AssignAdministrativeRoleUseCase,
        RevokeAdministrativeRoleUseCase, ListUserRoleAssignmentsUseCase, ListAdministrationAuditUseCase,
        ListAdministrativeRoleTemplatesUseCase, ProvisionAdministrativeRoleTemplatesUseCase,
        CloneAdministrativeRoleUseCase, GetAdministrativePlatformOptionsUseCase, UpdateAdministrativePlatformOptionsUseCase,
        ListBusinessActorGovernanceUseCase, GovernBusinessActorUseCase,
        ListGovernedOrganizationsUseCase, GovernOrganizationUseCase,
        ListGovernedAgenciesUseCase, GovernAgencyUseCase,
        ListTenantUsersUseCase {

    private static final Set<String> RESERVED_ROLE_CODES = Set.of(
            "GENERAL_ADMIN", "SYSTEM_ADMIN", "IAM_ADMIN", "TENANT_ADMIN",
            "ORGANIZATION_ADMIN", "AGENCY_ADMIN", "SALES_MANAGER", "INVENTORY_MANAGER",
            "ACCOUNTANT", "TREASURY_OFFICER", "RESOURCE_MANAGER", "HR_MANAGER",
            "PAYROLL_MANAGER", "BLOCKCHAIN_OPERATOR",
            // HRM-specific roles consumed by the HR Core frontend
            "SUPER_ADMIN", "HR_ADMIN", "HR_DIRECTOR", "MANAGER", "EMPLOYEE",
            "RECRUITER", "OCCUPATIONAL_DOCTOR", "HR_CONTROLLER");
    private static final Set<String> HRM_ALL_PERMISSIONS = Set.of(
            "hrm:employee:create", "hrm:employee:read", "hrm:employee:update", "hrm:employee:terminate",
            "hrm:employee:suspend", "hrm:employee:reactivate", "hrm:contract:create", "hrm:contract:read",
            "hrm:dependent:create", "hrm:dependent:read", "hrm:onboarding:create", "hrm:onboarding:read",
            "hrm:onboarding:manage", "hrm:recruitment:create", "hrm:recruitment:read",
            "hrm:recruitment:manage", "hrm:payroll:run", "hrm:payroll:validate", "hrm:payroll:read",
            "hrm:leave:create", "hrm:leave:approve", "hrm:leave:read", "hrm:timesheet:create",
            "hrm:timesheet:validate", "hrm:timesheet:read", "hrm:expense:create", "hrm:expense:manage",
            "hrm:expense:read", "hrm:loan:create", "hrm:loan:approve", "hrm:loan:read",
            "hrm:mission:create", "hrm:mission:manage", "hrm:mission:read", "hrm:mission:accept",
            "hrm:medical:create",
            "hrm:medical:read", "hrm:review:create", "hrm:review:manage", "hrm:review:read",
            "hrm:training:create", "hrm:training:manage", "hrm:training:read", "hrm:budget:create",
            "hrm:budget:manage", "hrm:budget:read", "hrm:skill:create", "hrm:skill:read",
            "hrm:declaration:create", "hrm:declaration:manage", "hrm:declaration:read",
            "hrm:kpi:create", "hrm:kpi:read");
    private static final Set<String> HRM_AGENCY_OPERATIONS = Set.of(
            "hrm:leave:create", "hrm:leave:approve", "hrm:leave:read", "hrm:timesheet:create",
            "hrm:timesheet:validate", "hrm:timesheet:read", "hrm:expense:create", "hrm:expense:manage",
            "hrm:expense:read", "hrm:mission:create", "hrm:mission:manage", "hrm:mission:read",
            "hrm:mission:accept",
            "hrm:training:create", "hrm:training:manage", "hrm:training:read", "hrm:skill:create",
            "hrm:skill:read");
    private static final Set<String> PAYROLL_PERMISSIONS = Set.of(
            "hrm:employee:read", "hrm:contract:read", "hrm:dependent:read", "hrm:payroll:run",
            "hrm:payroll:validate", "hrm:payroll:read", "hrm:leave:read", "hrm:timesheet:read",
            "hrm:expense:read", "hrm:loan:read", "hrm:kpi:read");
    /** Manager : approves/validates for their team, manages missions and expenses. */
    private static final Set<String> MANAGER_PERMISSIONS = Set.of(
            "hrm:employee:read", "hrm:contract:read", "hrm:dependent:read",
            "hrm:leave:read", "hrm:leave:approve",
            "hrm:timesheet:read", "hrm:timesheet:validate",
            "hrm:mission:create", "hrm:mission:manage", "hrm:mission:read", "hrm:mission:accept",
            "hrm:expense:read", "hrm:expense:manage",
            "hrm:review:create", "hrm:review:manage", "hrm:review:read",
            "hrm:training:read",
            "hrm:skill:read",
            "hrm:recruitment:read", "hrm:recruitment:manage");
    /** Employee : self-service capabilities. Reads on own records are scoped server-side. */
    private static final Set<String> EMPLOYEE_PERMISSIONS = Set.of(
            "hrm:employee:read", "hrm:contract:read", "hrm:dependent:read",
            "hrm:leave:create", "hrm:leave:read",
            "hrm:loan:create", "hrm:loan:read",
            "hrm:expense:create", "hrm:expense:read",
            "hrm:timesheet:create", "hrm:timesheet:read",
            "hrm:mission:read", "hrm:mission:accept",
            "hrm:training:read",
            "hrm:review:read",
            "hrm:medical:read",
            "hrm:payroll:read",
            "hrm:skill:read");
    /** Recruiter : full recruitment + onboarding + can create new employees on hire. */
    private static final Set<String> RECRUITER_PERMISSIONS = Set.of(
            "hrm:recruitment:create", "hrm:recruitment:read", "hrm:recruitment:manage",
            "hrm:onboarding:create", "hrm:onboarding:read", "hrm:onboarding:manage",
            "hrm:employee:create", "hrm:employee:read", "hrm:contract:create", "hrm:contract:read",
            "hrm:skill:read");
    /** Occupational doctor : medical follow-up only. */
    private static final Set<String> OCCUPATIONAL_DOCTOR_PERMISSIONS = Set.of(
            "hrm:medical:create", "hrm:medical:read",
            "hrm:employee:read", "hrm:contract:read", "hrm:dependent:read");
    /** HR controller : read-only across the entire HRM perimeter for reporting + analytics. */
    private static final Set<String> HR_CONTROLLER_PERMISSIONS = Set.of(
            "hrm:employee:read", "hrm:contract:read", "hrm:dependent:read",
            "hrm:leave:read", "hrm:timesheet:read", "hrm:mission:read",
            "hrm:payroll:read", "hrm:loan:read", "hrm:expense:read",
            "hrm:review:read", "hrm:training:read", "hrm:budget:read",
            "hrm:skill:read", "hrm:medical:read", "hrm:recruitment:read",
            "hrm:onboarding:read", "hrm:declaration:read", "hrm:kpi:read", "hrm:kpi:create");
    /** HR director / DRH : strategic management — full HRM + training/budget governance + analytics. */
    private static final Set<String> HR_DIRECTOR_PERMISSIONS = Set.of(
            "hrm:employee:read", "hrm:contract:read", "hrm:dependent:read",
            "hrm:training:create", "hrm:training:manage", "hrm:training:read",
            "hrm:budget:create", "hrm:budget:manage", "hrm:budget:read",
            "hrm:review:create", "hrm:review:manage", "hrm:review:read",
            "hrm:skill:create", "hrm:skill:read",
            "hrm:recruitment:create", "hrm:recruitment:read", "hrm:recruitment:manage",
            "hrm:onboarding:create", "hrm:onboarding:read", "hrm:onboarding:manage",
            "hrm:medical:read",
            "hrm:declaration:read",
            "hrm:kpi:create", "hrm:kpi:read",
            "hrm:leave:read", "hrm:timesheet:read", "hrm:payroll:read");
    private static final Set<String> BLOCKCHAIN_PERMISSIONS = Set.of(
            "blockchain:wallet:create", "blockchain:wallet:read", "blockchain:transaction:sign",
            "blockchain:transaction:create", "blockchain:transaction:read", "blockchain:anchor:create",
            "blockchain:block:mine", "blockchain:block:read", "blockchain:chain:validate");

    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository assignmentRepository;
    private final UserAccountRepository userAccountRepository;
    private final BusinessActorProfileRepository businessActorProfileRepository;
    private final OrganizationRepository organizationRepository;
    private final AgencyRepository agencyRepository;
    private final AdministrativePlatformOptionsRepository administrativePlatformOptionsRepository;
    private final AdminAuditRepository adminAuditRepository;
    private final PermissionCatalogService permissionCatalogService;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;
    private final Set<AdministrativeRoleTemplate> defaultRoleTemplates = Set.of(
            template("GENERAL_ADMIN", "General Administrator", "TENANT",
                    merge(Set.of("administration:read", "administration:write", "administration:roles:read",
                            "administration:roles:write", "administration:roles:clone",
                            "administration:permissions:read", "administration:assignments:write",
                            "administration:settings:read", "administration:settings:write",
                            "administration:audit:read", "administration:govern:business-actors",
                            "administration:govern:organizations", "administration:govern:agencies",
                            "organizations:write", "third-parties:write", "products:write", "inventory:write",
                            "sales:write", "accounting:read", "accounting:write", "accounting:post", "treasury:manage",
                            "treasury:read", "treasury:settle-invoices", "resources:write", "resources:reserve",
                            "resources:unassign", "resources:dispose", "cashier:write", "settings:read",
                            "settings:write", "tenant:admin"), HRM_ALL_PERMISSIONS, BLOCKCHAIN_PERMISSIONS),
                    true),
            template("ORGANIZATION_ADMIN", "Organization Administrator", "ORGANIZATION",
                    merge(Set.of("administration:read", "administration:roles:read", "administration:roles:write",
                            "administration:roles:clone", "administration:permissions:read",
                            "administration:assignments:write", "administration:settings:read",
                            "administration:audit:read", "administration:govern:agencies",
                            "organizations:write", "third-parties:write", "products:write", "inventory:write",
                            "sales:write", "accounting:read", "accounting:write", "accounting:post", "treasury:manage",
                            "treasury:read", "treasury:settle-invoices", "resources:write", "resources:reserve",
                            "resources:unassign", "resources:dispose", "cashier:write", "settings:read",
                            "settings:write"), HRM_ALL_PERMISSIONS, BLOCKCHAIN_PERMISSIONS),
                    false),
            template("AGENCY_ADMIN", "Agency Administrator", "AGENCY",
                    merge(Set.of("administration:read", "administration:roles:read", "administration:assignments:write",
                            "inventory:write", "sales:write", "resources:write", "resources:reserve",
                            "resources:unassign", "resources:dispose", "treasury:read", "cashier:write"),
                            HRM_AGENCY_OPERATIONS),
                    false),
            template("SALES_MANAGER", "Sales Manager", "ORGANIZATION",
                    Set.of("sales:write", "third-parties:write", "products:write"), false),
            template("INVENTORY_MANAGER", "Inventory Manager", "AGENCY",
                    Set.of("inventory:write", "products:write"), false),
            template("ACCOUNTANT", "Accountant", "ORGANIZATION",
                    Set.of("accounting:read", "accounting:write", "accounting:post"), false),
            template("TREASURY_OFFICER", "Treasury Officer", "ORGANIZATION",
                    Set.of("treasury:manage", "treasury:read", "treasury:settle-invoices"), false),
            template("RESOURCE_MANAGER", "Resource Manager", "AGENCY",
                    Set.of("resources:write", "resources:reserve", "resources:unassign", "resources:dispose"), false),
            template("HR_MANAGER", "Human Resources Manager", "ORGANIZATION", HRM_ALL_PERMISSIONS, false),
            template("PAYROLL_MANAGER", "Payroll Manager", "ORGANIZATION", PAYROLL_PERMISSIONS, false),
            template("BLOCKCHAIN_OPERATOR", "Blockchain Operator", "ORGANIZATION", BLOCKCHAIN_PERMISSIONS, false),
            // HRM-specific role templates consumed by the HR Core frontend
            template("SUPER_ADMIN", "Super Administrator", "TENANT",
                    merge(Set.of("administration:read", "administration:write",
                            "administration:roles:read", "administration:roles:write",
                            "administration:roles:clone", "administration:permissions:read",
                            "administration:assignments:write", "administration:settings:read",
                            "administration:settings:write", "administration:audit:read",
                            "administration:govern:business-actors", "administration:govern:organizations",
                            "administration:govern:agencies",
                            "organizations:write", "settings:read", "settings:write", "tenant:admin"),
                            HRM_ALL_PERMISSIONS),
                    true),
            template("HR_ADMIN", "HR Administrator", "ORGANIZATION", HRM_ALL_PERMISSIONS, false),
            template("HR_DIRECTOR", "HR Director (DRH)", "ORGANIZATION", HR_DIRECTOR_PERMISSIONS, false),
            template("MANAGER", "Team Manager", "ORGANIZATION", MANAGER_PERMISSIONS, false),
            template("EMPLOYEE", "Employee (self-service)", "ORGANIZATION", EMPLOYEE_PERMISSIONS, false),
            template("RECRUITER", "Recruiter", "ORGANIZATION", RECRUITER_PERMISSIONS, false),
            template("OCCUPATIONAL_DOCTOR", "Occupational Doctor", "ORGANIZATION",
                    OCCUPATIONAL_DOCTOR_PERMISSIONS, false),
            template("HR_CONTROLLER", "HR Management Controller", "ORGANIZATION",
                    HR_CONTROLLER_PERMISSIONS, false));

    public AdministrationApplicationService(RoleRepository roleRepository,
            UserRoleAssignmentRepository assignmentRepository,
            UserAccountRepository userAccountRepository,
            BusinessActorProfileRepository businessActorProfileRepository,
            OrganizationRepository organizationRepository,
            AgencyRepository agencyRepository,
            AdministrativePlatformOptionsRepository administrativePlatformOptionsRepository,
            AdminAuditRepository adminAuditRepository,
            PermissionCatalogService permissionCatalogService,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.roleRepository = roleRepository;
        this.assignmentRepository = assignmentRepository;
        this.userAccountRepository = userAccountRepository;
        this.businessActorProfileRepository = businessActorProfileRepository;
        this.organizationRepository = organizationRepository;
        this.agencyRepository = agencyRepository;
        this.administrativePlatformOptionsRepository = administrativePlatformOptionsRepository;
        this.adminAuditRepository = adminAuditRepository;
        this.permissionCatalogService = permissionCatalogService;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Flux<PermissionCatalogEntry> listPermissions() {
        return Flux.fromIterable(permissionCatalogService.list());
    }

    @Override
    public Flux<AdministrativeRoleTemplate> listRoleTemplates() {
        return Flux.fromIterable(defaultRoleTemplates)
                .sort(Comparator.comparing(AdministrativeRoleTemplate::code));
    }

    @Override
    public Flux<yowyob.comops.api.auth.domain.model.UserAccount> listTenantUsers(UUID tenantId) {
        return userAccountRepository.findAllByTenantId(tenantId);
    }

    @Override
    public Flux<Role> listRoles(UUID tenantId) {
        return roleRepository.findByTenantId(tenantId)
                .sort(Comparator.comparing(Role::code));
    }

    @Override
    public Mono<Role> getRole(UUID tenantId, UUID roleId) {
        return roleRepository.findById(tenantId, roleId);
    }

    @Override
    public Mono<Role> createRole(CreateAdministrativeRoleCommand command) {
        Objects.requireNonNull(command, "command is required");
        RoleScopeType scopeType = RoleScopeType.from(command.scopeType());
        Mono<Role> operation = getPlatformOptions(command.tenantId())
                .flatMap(options -> {
                    ensureCustomRoleScopeAllowed(scopeType, options);
                    Set<String> normalizedPermissions = validatePermissions(command.permissions(),
                            command.allowProtectedPermissions(), scopeType);
                    Role role = Role.create(command.tenantId(), command.code(), command.name(), scopeType, normalizedPermissions);
                    validateRoleCode(role.code(), command.allowProtectedPermissions());
                    return roleRepository.existsByCode(role.tenantId(), role.code())
                            .flatMap(exists -> exists ? Mono.error(new DuplicateRoleCodeException(role.code()))
                                    : roleRepository.save(role))
                            .flatMap(saved -> audit(command.tenantId(), command.organizationId(), command.actorUserId(),
                                    "ROLE_CREATED", "ROLE", saved.id().toString(),
                                    "code=" + saved.code() + ", scopeType=" + saved.scopeType().name()
                                            + ", permissions=" + String.join(",", saved.permissions()))
                                    .then(publish("ADMIN_ROLE_CREATED", command.tenantId(), command.organizationId(), saved.id(), payload(
                                            "code", saved.code(),
                                            "scopeType", saved.scopeType().name()))
                                            .thenReturn(saved)));
                });
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<Role> cloneRole(CloneAdministrativeRoleCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Role> operation = getPlatformOptions(command.tenantId())
                .flatMap(options -> {
                    if (!options.allowRoleCloning()) {
                        return Mono.error(new IllegalStateException("role cloning is disabled by platform options"));
                    }
                    return roleRepository.findById(command.tenantId(), command.sourceRoleId())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Source role not found.")))
                            .flatMap(source -> {
                                ensureRoleCanBeCloned(source, command.allowProtectedPermissions());
                                RoleScopeType targetScope = command.scopeType() == null || command.scopeType().isBlank()
                                        ? source.scopeType()
                                        : RoleScopeType.from(command.scopeType());
                                ensureCustomRoleScopeAllowed(targetScope, options);
                                Set<String> normalizedPermissions = validatePermissions(source.permissions(),
                                        command.allowProtectedPermissions(), targetScope);
                                validateRoleCode(command.code(), command.allowProtectedPermissions());
                                Role clone = Role.create(command.tenantId(), command.code(), command.name(),
                                        targetScope, normalizedPermissions);
                                return roleRepository.existsByCode(command.tenantId(), clone.code())
                                        .flatMap(exists -> exists
                                                ? Mono.<Role>error(new DuplicateRoleCodeException(clone.code()))
                                                : roleRepository.save(clone))
                                        .flatMap(saved -> audit(command.tenantId(), command.organizationId(), command.actorUserId(),
                                                "ROLE_CLONED", "ROLE", saved.id().toString(),
                                                "sourceRoleId=" + source.id() + ", code=" + saved.code())
                                                .then(publish("ADMIN_ROLE_CLONED", command.tenantId(), command.organizationId(),
                                                        saved.id(), payload("sourceRoleId", source.id(), "code", saved.code()))
                                                        .thenReturn(saved)));
                            });
                });
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<Role> updateRole(UpdateAdministrativeRoleCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Role> operation = roleRepository.findById(command.tenantId(), command.roleId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found.")))
                .flatMap(existing -> {
                    ensureRoleCanBeMutated(existing, command.allowProtectedMutations());
                    Role updated = existing.rename(command.name());
                    return roleRepository.save(updated)
                            .flatMap(saved -> audit(command.tenantId(), command.organizationId(), command.actorUserId(),
                                    "ROLE_UPDATED", "ROLE", saved.id().toString(), "name=" + saved.name())
                                    .then(publish("ADMIN_ROLE_UPDATED", command.tenantId(), command.organizationId(),
                                            saved.id(), payload("name", saved.name()))
                                            .thenReturn(saved)));
                });
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<Role> replacePermissions(ReplaceRolePermissionsCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Role> operation = roleRepository.findById(command.tenantId(), command.roleId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found.")))
                .flatMap(existing -> {
                    Set<String> normalizedPermissions = validatePermissions(command.permissions(),
                            command.allowProtectedPermissions(), existing.scopeType());
                    ensureRoleCanBeMutated(existing, command.allowProtectedPermissions());
                    Role updated = existing.replacePermissions(normalizedPermissions);
                    return roleRepository.save(updated)
                            .flatMap(saved -> audit(command.tenantId(), command.organizationId(), command.actorUserId(),
                                    "ROLE_PERMISSIONS_REPLACED", "ROLE", saved.id().toString(),
                                    String.join(",", saved.permissions()))
                                    .then(publish("ROLE_PERMISSIONS_REPLACED", command.tenantId(), command.organizationId(),
                                            saved.id(), payload("permissions", saved.permissions()))
                                            .thenReturn(saved)));
                });
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<Void> deleteRole(DeleteAdministrativeRoleCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Void> operation = roleRepository.findById(command.tenantId(), command.roleId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found.")))
                .flatMap(existing -> {
                    ensureRoleCanBeMutated(existing, command.allowProtectedMutations());
                    return assignmentRepository.findByTenantIdAndRoleId(command.tenantId(), existing.id())
                            .hasElements()
                            .flatMap(hasAssignments -> hasAssignments
                                    ? Mono.error(new RoleStillAssignedException(existing.id()))
                                    : roleRepository.deleteById(command.tenantId(), existing.id())
                                            .then(audit(command.tenantId(), command.organizationId(), command.actorUserId(),
                                                    "ROLE_DELETED", "ROLE", existing.id().toString(),
                                                    "code=" + existing.code()))
                                            .then(publish("ADMIN_ROLE_DELETED", command.tenantId(), command.organizationId(),
                                                    existing.id(), payload("code", existing.code()))));
                });
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<UserRoleAssignment> assignRole(AssignAdministrativeRoleCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<UserRoleAssignment> operation = Mono.zip(userAccountRepository.findById(command.tenantId(), command.userId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found."))),
                roleRepository.findById(command.tenantId(), command.roleId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found."))))
                .flatMap(tuple -> {
                    Role role = tuple.getT2();
                    RoleScopeType assignmentScopeType = resolveAssignmentScopeType(command, role);
                    UUID assignmentScopeId = resolveAssignmentScopeId(command, assignmentScopeType);
                    enforceAssignmentScope(role, assignmentScopeType, assignmentScopeId);
                    String legacyScope = UserRoleAssignment.assign(command.tenantId(), command.userId(), command.roleId(),
                            assignmentScopeType, assignmentScopeId).scope();
                    return assignmentRepository.findByTenantIdAndUserId(command.tenantId(), command.userId())
                            .filter(existing -> existing.roleId().equals(command.roleId()))
                            .filter(existing -> existing.scope().equalsIgnoreCase(legacyScope))
                            .hasElements()
                            .flatMap(alreadyAssigned -> alreadyAssigned
                                    ? Mono.<UserRoleAssignment>error(new IllegalArgumentException(
                                            "Role already assigned to user for this scope."))
                                    : assignmentRepository.save(UserRoleAssignment.assign(command.tenantId(), command.userId(),
                                            command.roleId(), assignmentScopeType, assignmentScopeId)));
                })
                .flatMap(saved -> audit(command.tenantId(), command.organizationId(), command.actorUserId(),
                        "ROLE_ASSIGNED", "USER_ROLE_ASSIGNMENT", saved.id().toString(),
                        "userId=" + saved.userId() + ", roleId=" + saved.roleId() + ", scope=" + saved.scope())
                        .then(publish("USER_ROLE_ASSIGNED", command.tenantId(), command.organizationId(), saved.id(),
                                payload("userId", saved.userId(), "roleId", saved.roleId(), "scope", saved.scope()))
                                .thenReturn(saved)));
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<Void> revokeRole(RevokeAdministrativeRoleCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Void> operation = assignmentRepository.findById(command.tenantId(), command.assignmentId())
                .switchIfEmpty(Mono.error(new UserRoleAssignmentNotFoundException(command.assignmentId())))
                .filter(existing -> existing.userId().equals(command.userId()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Assignment does not belong to user.")))
                .flatMap(existing -> assignmentRepository.deleteById(command.tenantId(), existing.id())
                        .then(audit(command.tenantId(), command.organizationId(), command.actorUserId(),
                                "ROLE_REVOKED", "USER_ROLE_ASSIGNMENT", existing.id().toString(),
                                "userId=" + existing.userId() + ", roleId=" + existing.roleId()))
                        .then(publish("USER_ROLE_REVOKED", command.tenantId(), command.organizationId(), existing.id(),
                                payload("userId", existing.userId(), "roleId", existing.roleId()))));
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Flux<UserRoleAssignment> listAssignments(UUID tenantId, UUID userId) {
        return assignmentRepository.findByTenantIdAndUserId(tenantId, userId)
                .sort(Comparator.comparing(UserRoleAssignment::createdAt));
    }

    @Override
    public Flux<AdminAuditEntry> listAudit(UUID tenantId, int limit) {
        int normalizedLimit = limit <= 0 ? 50 : Math.min(limit, 200);
        return adminAuditRepository.findByTenantId(tenantId, normalizedLimit);
    }

    @Override
    public Flux<Role> provisionDefaultRoles(UUID tenantId, UUID organizationId, UUID actorUserId,
            boolean allowProtectedPermissions) {
        return Flux.fromIterable(defaultRoleTemplates)
                .filter(template -> allowProtectedPermissions
                        || template.permissions().stream().noneMatch(permissionCatalogService::isProtected))
                .concatMap(template -> roleRepository.existsByCode(tenantId, template.code())
                        .flatMap(exists -> exists
                                ? Mono.<Role>empty()
                                : roleRepository.save(Role.create(tenantId, template.code(), template.name(),
                                                RoleScopeType.from(template.scopeType()), template.permissions()))
                                        .flatMap(saved -> audit(tenantId, organizationId, actorUserId,
                                                "ROLE_TEMPLATE_PROVISIONED", "ROLE", saved.id().toString(),
                                                "code=" + saved.code() + ", scopeType=" + saved.scopeType().name())
                                                .then(publish("ROLE_TEMPLATE_PROVISIONED", tenantId, organizationId,
                                                        saved.id(), payload("code", saved.code(), "scopeType", saved.scopeType().name()))
                                                        .thenReturn(saved)))));
    }

    @Override
    public Mono<AdministrativePlatformOptions> getPlatformOptions(UUID tenantId) {
        return administrativePlatformOptionsRepository.findByTenantId(tenantId)
                .switchIfEmpty(Mono.defer(() -> administrativePlatformOptionsRepository
                        .save(AdministrativePlatformOptions.defaults(tenantId))));
    }

    @Override
    public Mono<AdministrativePlatformOptions> updatePlatformOptions(UpdateAdministrativePlatformOptionsCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<AdministrativePlatformOptions> operation = getPlatformOptions(command.tenantId())
                .map(existing -> existing.update(command.requireBusinessActorApproval(), command.requireOrganizationApproval(),
                        command.allowOrganizationSelfServiceCreation(), command.allowAgencySelfServiceCreation(),
                        command.allowRoleCloning(), command.allowAgencyScopedCustomRoles(),
                        command.allowOrganizationAdminsToGovernAgencies(), command.allowBusinessActorSelfReactivation()))
                .flatMap(administrativePlatformOptionsRepository::save)
                .flatMap(saved -> audit(command.tenantId(), command.organizationId(), command.actorUserId(),
                        "ADMIN_PLATFORM_OPTIONS_UPDATED", "ADMIN_PLATFORM_OPTIONS", saved.id().toString(),
                        "roleCloning=" + saved.allowRoleCloning() + ", requireOrganizationApproval="
                                + saved.requireOrganizationApproval())
                        .then(publish("ADMIN_SETTINGS_UPDATED", command.tenantId(), command.organizationId(), saved.id(),
                                payload("allowRoleCloning", saved.allowRoleCloning(),
                                        "requireOrganizationApproval", saved.requireOrganizationApproval()))
                                .thenReturn(saved)));
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Flux<BusinessActorProfile> listBusinessActors(UUID tenantId, String status) {
        String normalizedStatus = normalizeStatus(status);
        return businessActorProfileRepository.findByTenantId(tenantId)
                .filter(profile -> normalizedStatus == null
                        || profile.governanceStatus().name().equalsIgnoreCase(normalizedStatus))
                .sort(Comparator.comparing(BusinessActorProfile::createdAt));
    }

    @Override
    public Mono<BusinessActorProfile> governBusinessActor(GovernBusinessActorCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<BusinessActorProfile> operation = businessActorProfileRepository.findById(command.tenantId(), command.businessActorId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Business actor not found.")))
                .flatMap(existing -> {
                    BusinessActorProfile updated = switch (normalizeAction(command.action())) {
                        case "APPROVE" -> existing.approve(command.actorUserId(), command.reason());
                        case "REJECT" -> existing.reject(command.actorUserId(), command.reason());
                        case "SUSPEND" -> existing.suspend(command.actorUserId(), command.reason());
                        case "BLOCK" -> existing.block(command.actorUserId(), command.reason());
                        case "REACTIVATE" -> existing.reactivate(command.actorUserId(), command.reason());
                        default -> throw new IllegalArgumentException("Unsupported business actor governance action: " + command.action());
                    };
                    return businessActorProfileRepository.save(updated)
                            .flatMap(saved -> audit(command.tenantId(), command.organizationId(), command.actorUserId(),
                                    "BUSINESS_ACTOR_" + saved.governanceStatus().name(), "BUSINESS_ACTOR",
                                    saved.id().toString(), "status=" + saved.governanceStatus().name())
                                    .then(publish("BUSINESS_ACTOR_" + saved.governanceStatus().name(), command.tenantId(),
                                            command.organizationId(), saved.id(), payload("status", saved.governanceStatus().name(),
                                                    "actorId", saved.actorId(), "reason", saved.governanceReason()))
                                            .thenReturn(saved)));
                });
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Flux<Organization> listOrganizations(UUID tenantId, String status) {
        String normalizedStatus = normalizeStatus(status);
        return organizationRepository.findByTenantId(tenantId)
                .filter(organization -> normalizedStatus == null
                        || organization.governanceStatus().name().equalsIgnoreCase(normalizedStatus))
                .sort(Comparator.comparing(Organization::createdAt));
    }

    @Override
    public Mono<Organization> governOrganization(GovernOrganizationCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Organization> operation = organizationRepository.findById(command.tenantId(), command.organizationId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Organization not found.")))
                .flatMap(existing -> {
                    Organization updated = switch (normalizeAction(command.action())) {
                        case "APPROVE" -> existing.approve(command.actorUserId(), command.reason());
                        case "REJECT" -> existing.reject(command.actorUserId(), command.reason());
                        case "SUSPEND" -> existing.suspend(command.actorUserId(), command.reason());
                        case "CLOSE" -> existing.close(command.actorUserId(), command.reason());
                        case "REOPEN" -> existing.reopen(command.actorUserId(), command.reason());
                        default -> throw new IllegalArgumentException("Unsupported organization governance action: " + command.action());
                    };
                    return organizationRepository.save(updated)
                            .flatMap(saved -> audit(command.tenantId(), saved.id(), command.actorUserId(),
                                    "ORGANIZATION_" + saved.governanceStatus().name(), "ORGANIZATION", saved.id().toString(),
                                    "status=" + saved.governanceStatus().name())
                                    .then(publish("ORGANIZATION_" + saved.governanceStatus().name(), command.tenantId(),
                                            saved.id(), saved.id(), payload("status", saved.governanceStatus().name(),
                                                    "businessActorId", saved.businessActorId(), "reason", saved.governanceReason()))
                                            .thenReturn(saved)));
                });
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Flux<Agency> listAgencies(UUID tenantId, UUID organizationId, String status) {
        String normalizedStatus = normalizeStatus(status);
        Flux<Agency> source = organizationId == null
                ? agencyRepository.findByTenantId(tenantId)
                : agencyRepository.findByOrganizationId(tenantId, organizationId);
        return source.filter(agency -> normalizedStatus == null
                        || agency.governanceStatus().name().equalsIgnoreCase(normalizedStatus))
                .sort(Comparator.comparing(Agency::createdAt));
    }

    @Override
    public Mono<Agency> governAgency(GovernAgencyCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Agency> operation = getPlatformOptions(command.tenantId())
                .flatMap(options -> agencyRepository.findById(command.tenantId(), command.agencyId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Agency not found.")))
                        .flatMap(existing -> {
                            if (command.organizationId() != null && !existing.organizationId().equals(command.organizationId())) {
                                return Mono.error(new IllegalArgumentException("Agency does not belong to the current organization scope."));
                            }
                            if (command.organizationId() != null && !options.allowOrganizationAdminsToGovernAgencies()) {
                                return Mono.error(new IllegalStateException("organization-level agency governance is disabled by platform options"));
                            }
                            Agency updated = switch (normalizeAction(command.action())) {
                                case "ACTIVATE" -> existing.activate(command.actorUserId(), command.reason());
                                case "SUSPEND" -> existing.suspend(command.actorUserId(), command.reason());
                                case "CLOSE" -> existing.close(command.actorUserId(), command.reason());
                                default -> throw new IllegalArgumentException("Unsupported agency governance action: " + command.action());
                            };
                            return agencyRepository.save(updated)
                                    .flatMap(saved -> audit(command.tenantId(), saved.organizationId(), command.actorUserId(),
                                            "AGENCY_" + saved.governanceStatus().name(), "AGENCY", saved.id().toString(),
                                            "status=" + saved.governanceStatus().name())
                                            .then(publish("AGENCY_" + saved.governanceStatus().name(), command.tenantId(),
                                                    saved.organizationId(), saved.id(), payload("status", saved.governanceStatus().name(),
                                                            "organizationId", saved.organizationId(), "reason", saved.governanceReason()))
                                                    .thenReturn(saved)));
                        }));
        return transactionalExecutor.transactional(operation);
    }

    private Set<String> validatePermissions(Set<String> permissions, boolean allowProtectedPermissions,
            RoleScopeType roleScopeType) {
        if (permissions == null || permissions.isEmpty()) {
            throw new PermissionCatalogValidationException("At least one permission is required.");
        }
        Set<String> normalized = permissions.stream()
                .filter(Objects::nonNull)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        if (normalized.isEmpty()) {
            throw new PermissionCatalogValidationException("At least one permission is required.");
        }
        for (String permission : normalized) {
            PermissionCatalogEntry entry = permissionCatalogService.get(permission);
            if (entry == null) {
                throw new PermissionCatalogValidationException("Unknown permission: " + permission);
            }
            if (!entry.assignable() && !allowProtectedPermissions) {
                throw new PermissionCatalogValidationException("Permission is protected and cannot be assigned: " + permission);
            }
            if (!scopeAllowsPermission(roleScopeType, entry.scope())) {
                throw new PermissionCatalogValidationException(
                        "Permission " + permission + " exceeds the role scope " + roleScopeType.name());
            }
        }
        return Set.copyOf(normalized);
    }

    private boolean scopeAllowsPermission(RoleScopeType roleScopeType, String permissionScope) {
        return scopeLevel(roleScopeType) <= scopeLevel(RoleScopeType.from(permissionScope));
    }

    private void ensureCustomRoleScopeAllowed(RoleScopeType scopeType, AdministrativePlatformOptions options) {
        if (scopeType == RoleScopeType.AGENCY && !options.allowAgencyScopedCustomRoles()) {
            throw new IllegalStateException("agency scoped custom roles are disabled by platform options");
        }
    }

    private int scopeLevel(RoleScopeType scopeType) {
        return switch (scopeType) {
            case SYSTEM -> 0;
            case TENANT -> 1;
            case ORGANIZATION -> 2;
            case AGENCY -> 3;
        };
    }

    private void ensureRoleCanBeMutated(Role role, boolean allowProtectedMutations) {
        if (allowProtectedMutations) {
            return;
        }
        String normalizedCode = role.code().trim().toUpperCase(Locale.ROOT);
        if (RESERVED_ROLE_CODES.contains(normalizedCode)) {
            throw new ProtectedRoleMutationException("Role " + role.code() + " is protected.");
        }
        boolean containsProtectedPermission = role.permissions().stream().anyMatch(permissionCatalogService::isProtected);
        if (containsProtectedPermission) {
            throw new ProtectedRoleMutationException("Role " + role.code() + " carries protected permissions.");
        }
    }

    private void ensureRoleCanBeCloned(Role role, boolean allowProtectedMutations) {
        if (allowProtectedMutations) {
            return;
        }
        boolean containsProtectedPermission = role.permissions().stream().anyMatch(permissionCatalogService::isProtected);
        if (containsProtectedPermission) {
            throw new ProtectedRoleMutationException("Role " + role.code() + " carries protected permissions and cannot be cloned.");
        }
    }

    private void validateRoleCode(String code, boolean allowProtectedMutations) {
        String normalizedCode = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        if (!allowProtectedMutations && RESERVED_ROLE_CODES.contains(normalizedCode)) {
            throw new ProtectedRoleMutationException("Role code " + code + " is reserved.");
        }
    }

    private RoleScopeType resolveAssignmentScopeType(AssignAdministrativeRoleCommand command, Role role) {
        if (command.scopeType() != null && !command.scopeType().isBlank()) {
            return RoleScopeType.from(command.scopeType());
        }
        if (command.scope() != null && !command.scope().isBlank()) {
            String normalized = command.scope().trim().toUpperCase(Locale.ROOT);
            if (normalized.startsWith("ORGANIZATION")) {
                return RoleScopeType.ORGANIZATION;
            }
            if (normalized.startsWith("AGENCY")) {
                return RoleScopeType.AGENCY;
            }
            if (normalized.startsWith("SYSTEM")) {
                return RoleScopeType.SYSTEM;
            }
            return RoleScopeType.TENANT;
        }
        return role.scopeType();
    }

    private UUID resolveAssignmentScopeId(AssignAdministrativeRoleCommand command, RoleScopeType scopeType) {
        if (command.scopeId() != null) {
            return command.scopeId();
        }
        return switch (scopeType) {
            case SYSTEM, TENANT -> null;
            case ORGANIZATION, AGENCY -> extractScopeId(command.scope());
        };
    }

    private UUID extractScopeId(String scope) {
        if (scope == null || scope.isBlank() || !scope.contains(":")) {
            return null;
        }
        return UUID.fromString(scope.substring(scope.indexOf(':') + 1));
    }

    private void enforceAssignmentScope(Role role, RoleScopeType assignmentScopeType, UUID assignmentScopeId) {
        if (role.scopeType() != assignmentScopeType) {
            throw new IllegalArgumentException("Role scope " + role.scopeType().name()
                    + " cannot be assigned with scope " + assignmentScopeType.name());
        }
        if ((assignmentScopeType == RoleScopeType.ORGANIZATION || assignmentScopeType == RoleScopeType.AGENCY)
                && assignmentScopeId == null) {
            throw new IllegalArgumentException("scopeId is required for " + assignmentScopeType.name() + " assignments");
        }
    }

    private Mono<AdminAuditEntry> audit(UUID tenantId, UUID organizationId, UUID actorUserId, String action,
            String targetType, String targetId, String payloadSummary) {
        return adminAuditRepository.save(AdminAuditEntry.record(tenantId, organizationId, actorUserId, action,
                targetType, targetId, payloadSummary));
    }

    private Mono<Void> publish(String eventType, UUID tenantId, UUID organizationId, UUID aggregateId,
            Map<String, Object> payload) {
        return businessEventPublisher.publish(BusinessEvent.now(tenantId, organizationId, eventType,
                "ADMINISTRATION", aggregateId, payload));
    }

    private AdministrativeRoleTemplate template(String code, String name, String scopeType, Set<String> permissions,
            boolean protectedTemplate) {
        return new AdministrativeRoleTemplate(code, name, scopeType, Set.copyOf(permissions), protectedTemplate);
    }

    @SafeVarargs
    private static Set<String> merge(Set<String>... permissionGroups) {
        Set<String> merged = new LinkedHashSet<>();
        for (Set<String> permissionGroup : permissionGroups) {
            merged.addAll(permissionGroup);
        }
        return Set.copyOf(merged);
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }

    private String normalizeAction(String action) {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("governance action is required");
        }
        return action.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? null : status.trim().toUpperCase(Locale.ROOT);
    }
}
