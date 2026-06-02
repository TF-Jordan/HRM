package yowyob.comops.api.bootstrap.integration.auth;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import yowyob.comops.api.auth.application.port.out.UserOrganizationAccess;
import yowyob.comops.api.auth.application.port.out.UserOrganizationAccessDirectory;
import yowyob.comops.api.organization.application.port.in.ListUserOrganizationAccessUseCase;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.roles.application.port.out.RoleRepository;
import yowyob.comops.api.roles.application.port.out.UserRoleAssignmentRepository;
import yowyob.comops.api.roles.domain.model.RoleScopeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregates a user's accessible organizations across three sources:
 *   1. organizations the user *owns* via a business_actor_profile,
 *   2. organizations the user is a *member* of (employee_membership),
 *   3. organizations granted *only* through a roles_core.user_role_assignment
 *      scoped to ORGANIZATION (e.g. a plain MANAGER user with no business-actor
 *      relationship in the org).
 *
 * For every resulting org we attach the user's role codes scoped to that org,
 * which the frontend uses to dispatch users to their role-specific namespace.
 *
 * Without (3) and the role codes, scoped-only users would log in with an empty
 * `organizations` array in `/api/auth/discover-contexts`, causing the frontend
 * to default everyone to the EMPLOYEE namespace.
 */
@Component
public class OrganizationCoreUserOrganizationAccessDirectory implements UserOrganizationAccessDirectory {

    private final ListUserOrganizationAccessUseCase listUserOrganizationAccessUseCase;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;

    public OrganizationCoreUserOrganizationAccessDirectory(
            ListUserOrganizationAccessUseCase listUserOrganizationAccessUseCase,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            RoleRepository roleRepository,
            OrganizationRepository organizationRepository) {
        this.listUserOrganizationAccessUseCase = listUserOrganizationAccessUseCase;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public Flux<UserOrganizationAccess> listUserOrganizations(UUID tenantId, UUID userId) {
        Mono<Map<UUID, List<String>>> rolesByOrgMono = roleCodesByOrganization(tenantId, userId).cache();

        Flux<UserOrganizationAccess> primary = listUserOrganizationAccessUseCase
                .listUserOrganizationAccess(tenantId, userId)
                .flatMap(view -> rolesByOrgMono.map(rolesByOrg -> new UserOrganizationAccess(
                        view.organizationId(),
                        view.organizationCode(),
                        view.shortName(),
                        view.longName(),
                        view.services(),
                        rolesByOrg.getOrDefault(view.organizationId(), List.of()))));

        return primary
                .collectList()
                .flatMapMany(already -> mergeRoleScopedOrganizations(tenantId, already, rolesByOrgMono));
    }

    /** Builds `organizationId -> [roleCode...]` from user_role_assignment for the user. */
    private Mono<Map<UUID, List<String>>> roleCodesByOrganization(UUID tenantId, UUID userId) {
        return userRoleAssignmentRepository.findByTenantIdAndUserId(tenantId, userId)
                .filter(assignment -> assignment.scopeType() == RoleScopeType.ORGANIZATION
                        && assignment.scopeId() != null)
                .flatMap(assignment -> roleRepository.findById(tenantId, assignment.roleId())
                        .map(role -> Map.entry(assignment.scopeId(), role.code())))
                .collect(LinkedHashMap::new, (acc, entry) -> {
                    @SuppressWarnings("unchecked")
                    Map<UUID, List<String>> map = (Map<UUID, List<String>>) (Map<?, ?>) acc;
                    map.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
                });
    }

    /**
     * For each org found only via role assignments (not yet in `already`), resolve
     * the organization metadata and emit a UserOrganizationAccess with the role
     * codes attached. Services list stays empty here — the frontend doesn't need
     * the subscription entitlements to route by role, and computing them per-org
     * for the role-scoped path would re-import organization-core's service.
     */
    private Flux<UserOrganizationAccess> mergeRoleScopedOrganizations(
            UUID tenantId,
            List<UserOrganizationAccess> already,
            Mono<Map<UUID, List<String>>> rolesByOrgMono) {
        Map<UUID, UserOrganizationAccess> byId = new HashMap<>();
        for (UserOrganizationAccess access : already) {
            byId.put(access.organizationId(), access);
        }
        return rolesByOrgMono
                .flatMapMany(rolesByOrg -> {
                    List<UUID> missing = rolesByOrg.keySet().stream()
                            .filter(orgId -> !byId.containsKey(orgId))
                            .toList();
                    Flux<UserOrganizationAccess> resolved = Flux.fromIterable(missing)
                            .flatMap(orgId -> organizationRepository.findById(tenantId, orgId)
                                    .map(org -> new UserOrganizationAccess(
                                            org.id(),
                                            org.code(),
                                            org.shortName(),
                                            org.longName(),
                                            List.of(),
                                            rolesByOrg.getOrDefault(org.id(), List.of()))));
                    return Flux.fromIterable(already).concatWith(resolved);
                });
    }
}
