package yowyob.comops.api.organization.application.service;

import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.port.in.AdminAddEmployeeMembershipCommand;
import yowyob.comops.api.organization.application.port.in.AdminAddEmployeeMembershipUseCase;
import yowyob.comops.api.organization.application.port.in.InviteEmployeeCommand;
import yowyob.comops.api.organization.application.port.in.InviteEmployeeUseCase;
import yowyob.comops.api.organization.application.port.in.ListEmployeesUseCase;
import yowyob.comops.api.organization.application.port.in.ListOrganizationRolesUseCase;
import yowyob.comops.api.organization.application.port.in.RemoveEmployeeUseCase;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.EmployeeMembershipRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRoleGateway;
import yowyob.comops.api.organization.application.port.out.OrganizationUserDirectory;
import yowyob.comops.api.organization.domain.AgencyNotFoundException;
import yowyob.comops.api.organization.domain.DuplicateEmployeeMembershipException;
import yowyob.comops.api.organization.domain.EmployeeMembershipNotFoundException;
import yowyob.comops.api.organization.domain.OrganizationNotFoundException;
import yowyob.comops.api.organization.domain.UserAccountNotFoundInOrganizationContextException;
import yowyob.comops.api.organization.domain.model.EmployeeMembership;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmployeeMembershipApplicationService
        implements InviteEmployeeUseCase, ListEmployeesUseCase, RemoveEmployeeUseCase,
        ListOrganizationRolesUseCase, AdminAddEmployeeMembershipUseCase {

    private final EmployeeMembershipRepository employeeMembershipRepository;
    private final OrganizationRepository organizationRepository;
    private final AgencyRepository agencyRepository;
    private final OrganizationUserDirectory organizationUserDirectory;
    private final OrganizationRoleGateway organizationRoleGateway;

    public EmployeeMembershipApplicationService(EmployeeMembershipRepository employeeMembershipRepository,
            OrganizationRepository organizationRepository, AgencyRepository agencyRepository,
            OrganizationUserDirectory organizationUserDirectory, OrganizationRoleGateway organizationRoleGateway) {
        this.employeeMembershipRepository = employeeMembershipRepository;
        this.organizationRepository = organizationRepository;
        this.agencyRepository = agencyRepository;
        this.organizationUserDirectory = organizationUserDirectory;
        this.organizationRoleGateway = organizationRoleGateway;
    }

    @Override
    public Mono<EmployeeMembership> invite(InviteEmployeeCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Void> organizationCheck = organizationRepository.findById(command.tenantId(), command.organizationId())
                .switchIfEmpty(Mono.error(new OrganizationNotFoundException(command.organizationId())))
                .then();
        Mono<Void> agencyCheck = command.agencyId() == null
                ? Mono.empty()
                : agencyRepository.findById(command.tenantId(), command.agencyId())
                        .filter(agency -> agency.organizationId().equals(command.organizationId()))
                        .switchIfEmpty(Mono.error(new AgencyNotFoundException(command.agencyId())))
                        .then();

        return Mono.when(organizationCheck, agencyCheck)
                .then(organizationUserDirectory.findByEmail(command.tenantId(), command.email())
                        .switchIfEmpty(Mono.error(new UserAccountNotFoundInOrganizationContextException(command.email()))))
                .flatMap(userRecord -> employeeMembershipRepository
                        .existsByOrganizationAndUser(command.tenantId(), command.organizationId(), userRecord.userId())
                        .flatMap(exists -> exists
                                ? Mono.<EmployeeMembership>error(new DuplicateEmployeeMembershipException(
                                        userRecord.userId(), command.organizationId()))
                                : employeeMembershipRepository.save(EmployeeMembership.invite(command.tenantId(),
                                        command.organizationId(), userRecord.userId(), userRecord.actorId(),
                                        userRecord.email(), command.agencyId(), command.roleId()))))
                .flatMap(membership -> assignRoleIfNeeded(command, membership));
    }

    @Override
    public Mono<EmployeeMembership> addMembership(AdminAddEmployeeMembershipCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Void> organizationCheck = organizationRepository.findById(command.tenantId(), command.organizationId())
                .switchIfEmpty(Mono.error(new OrganizationNotFoundException(command.organizationId())))
                .then();
        Mono<Void> agencyCheck = command.agencyId() == null
                ? Mono.empty()
                : agencyRepository.findById(command.tenantId(), command.agencyId())
                        .filter(agency -> agency.organizationId().equals(command.organizationId()))
                        .switchIfEmpty(Mono.error(new AgencyNotFoundException(command.agencyId())))
                        .then();

        return Mono.when(organizationCheck, agencyCheck)
                .then(employeeMembershipRepository.existsByOrganizationAndUser(
                        command.tenantId(), command.organizationId(), command.userId()))
                .flatMap(exists -> exists
                        ? Mono.<EmployeeMembership>error(new DuplicateEmployeeMembershipException(
                                command.userId(), command.organizationId()))
                        : employeeMembershipRepository.save(EmployeeMembership.invite(
                                command.tenantId(),
                                command.organizationId(),
                                command.userId(),
                                command.actorId(),
                                command.email(),
                                command.agencyId(),
                                command.roleId())));
    }

    @Override
    public Flux<EmployeeMembership> listEmployees(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> employeeMembershipRepository.findByOrganizationId(context.tenantId(), organizationId));
    }

    @Override
    public Mono<Void> remove(UUID membershipId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeMembershipRepository.findById(context.tenantId(), membershipId)
                        .switchIfEmpty(Mono.error(new EmployeeMembershipNotFoundException(membershipId)))
                        .map(EmployeeMembership::remove)
                        .flatMap(employeeMembershipRepository::save)
                        .then());
    }

    @Override
    public Flux<OrganizationRoleGateway.RoleRecord> listRoles() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> organizationRoleGateway.listRoles(context.tenantId()));
    }

    private Mono<EmployeeMembership> assignRoleIfNeeded(InviteEmployeeCommand command, EmployeeMembership membership) {
        if (command.permissions() != null && !command.permissions().isEmpty()) {
            String generatedCode = "ORG-" + membership.organizationId().toString().substring(0, 8).toUpperCase()
                    + "-USR-" + membership.userId().toString().substring(0, 8).toUpperCase();
            String generatedName = "Ad hoc role for " + membership.email();
            return organizationRoleGateway.createRole(command.tenantId(), generatedCode, generatedName,
                            "ORGANIZATION",
                            command.permissions().stream()
                                    .filter(permission -> permission != null && !permission.isBlank())
                                    .map(String::trim)
                                    .collect(Collectors.toList()))
                    .flatMap(role -> organizationRoleGateway.assignRole(command.tenantId(), membership.userId(), role.id(),
                            "ORGANIZATION", command.organizationId(), "ORGANIZATION:" + command.organizationId()))
                    .thenReturn(membership);
        }
        if (command.roleId() == null) {
            return Mono.just(membership);
        }
        return organizationRoleGateway.assignRole(command.tenantId(), membership.userId(), command.roleId(),
                        "ORGANIZATION", command.organizationId(), "ORGANIZATION:" + command.organizationId())
                .thenReturn(membership);
    }
}
