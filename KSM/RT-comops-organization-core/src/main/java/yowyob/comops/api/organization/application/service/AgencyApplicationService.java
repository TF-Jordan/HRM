package yowyob.comops.api.organization.application.service;

import yowyob.comops.api.organization.application.port.in.CreateAgencyCommand;
import yowyob.comops.api.organization.application.port.in.CreateAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.ActivateAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.CloseAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.DeactivateAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.ListAgenciesUseCase;
import yowyob.comops.api.organization.application.port.in.SuspendAgencyUseCase;
import yowyob.comops.api.organization.application.port.in.UpdateAgencyCommand;
import yowyob.comops.api.organization.application.port.in.UpdateAgencyUseCase;
import yowyob.comops.api.organization.application.port.out.AgencySelfServiceCreationPolicy;
import yowyob.comops.api.organization.application.port.out.OrganizationGovernanceAuditPort;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.domain.AgencySelfServiceCreationDisabledException;
import yowyob.comops.api.organization.domain.AgencyNotFoundException;
import yowyob.comops.api.organization.domain.DuplicateAgencyCodeException;
import yowyob.comops.api.organization.domain.OrganizationNotFoundException;
import yowyob.comops.api.organization.domain.model.Agency;
import yowyob.comops.api.file.application.port.out.StoredFileRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@Service
public class AgencyApplicationService
        implements CreateAgencyUseCase, ListAgenciesUseCase, UpdateAgencyUseCase, DeactivateAgencyUseCase,
        ActivateAgencyUseCase, SuspendAgencyUseCase, CloseAgencyUseCase {

    private final AgencyRepository agencyRepository;
    private final OrganizationRepository organizationRepository;
    private final StoredFileRepository storedFileRepository;
    private final Optional<AgencySelfServiceCreationPolicy> agencySelfServiceCreationPolicy;
    private final Optional<OrganizationGovernanceAuditPort> governanceAuditPort;

    public AgencyApplicationService(AgencyRepository agencyRepository, OrganizationRepository organizationRepository,
            StoredFileRepository storedFileRepository,
            Optional<AgencySelfServiceCreationPolicy> agencySelfServiceCreationPolicy,
            Optional<OrganizationGovernanceAuditPort> governanceAuditPort) {
        this.agencyRepository = agencyRepository;
        this.organizationRepository = organizationRepository;
        this.storedFileRepository = storedFileRepository;
        this.agencySelfServiceCreationPolicy = agencySelfServiceCreationPolicy;
        this.governanceAuditPort = governanceAuditPort;
    }

    @Override
    public Mono<Agency> createAgency(CreateAgencyCommand command) {
        Objects.requireNonNull(command, "command is required");
        return ensureSelfServiceCreationAllowed(command.tenantId())
                .then(validateLogoReference(command.tenantId(), command.logoId()))
                .then(organizationRepository.findById(command.tenantId(), command.organizationId()))
                .switchIfEmpty(Mono.error(new OrganizationNotFoundException(command.organizationId())))
                .flatMap(organization -> {
                    Agency agency = Agency.create(command.tenantId(), command.organizationId(), command.code(),
                            command.ownerId(), command.managerId(), command.name(), command.location(),
                            command.description(), command.transferable(), command.active(), command.logoUri(),
                            command.logoId(), command.shortName(), command.longName(),
                            command.isIndividualBusiness(), command.isHeadquarter(), command.country(),
                            command.city(), command.latitude(), command.longitude(), command.openTime(),
                            command.closeTime(), command.phone(), command.email(), command.whatsapp(),
                            command.greetingMessage(), command.averageRevenue(), command.capitalShare(),
                            command.registrationNumber(), command.socialNetwork(), command.taxNumber(),
                            command.keywords(), command.isPublic(), command.isBusiness(),
                            command.totalAffiliatedCustomers(), command.agencyType());
                    return agencyRepository.existsByCode(command.tenantId(), command.organizationId(), agency.code())
                            .flatMap(exists -> exists
                                    ? Mono.error(new DuplicateAgencyCodeException(agency.code()))
                                    : agencyRepository.save(agency));
                });
    }

    @Override
    public Flux<Agency> listAgencies(java.util.UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> agencyRepository.findByOrganizationId(context.tenantId(), organizationId));
    }

    @Override
    public Mono<Agency> updateAgency(UpdateAgencyCommand command) {
        Objects.requireNonNull(command, "command is required");
        return validateLogoReference(command.tenantId(), command.logoId())
                .then(agencyRepository.findById(command.tenantId(), command.agencyId()))
                .switchIfEmpty(Mono.error(new AgencyNotFoundException(command.agencyId())))
                .flatMap(existing -> agencyRepository.existsByCode(command.tenantId(), existing.organizationId(), command.code())
                        .flatMap(exists -> exists && !existing.code().equalsIgnoreCase(command.code())
                                ? Mono.<Agency>error(new DuplicateAgencyCodeException(command.code()))
                                : agencyRepository.save(existing.update(command.code(), command.ownerId(),
                                        command.managerId(), command.name(), command.location(),
                                        command.description(), command.transferable(), command.active(),
                                        command.logoUri(), command.logoId(), command.shortName(),
                                        command.longName(), command.isIndividualBusiness(),
                                        command.isHeadquarter(), command.country(), command.city(),
                                        command.latitude(), command.longitude(), command.openTime(),
                                        command.closeTime(), command.phone(), command.email(),
                                        command.whatsapp(), command.greetingMessage(),
                                        command.averageRevenue(), command.capitalShare(),
                                        command.registrationNumber(), command.socialNetwork(),
                                        command.taxNumber(), command.keywords(), command.isPublic(),
                                        command.isBusiness(), command.totalAffiliatedCustomers(),
                                        command.agencyType()))));
    }

    @Override
    public Mono<Void> deactivateAgency(java.util.UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> agencyRepository.findById(context.tenantId(), agencyId)
                        .switchIfEmpty(Mono.error(new AgencyNotFoundException(agencyId)))
                        .map(Agency::deactivate)
                        .flatMap(agencyRepository::save)
                        .then());
    }

    @Override
    public Mono<Void> activateAgency(java.util.UUID tenantId, java.util.UUID organizationId, java.util.UUID agencyId,
            java.util.UUID adminUserId, String reason) {
        return governAgency(tenantId, organizationId, agencyId, adminUserId, reason, AgencyGovernanceAction.ACTIVATE);
    }

    @Override
    public Mono<Void> suspendAgency(java.util.UUID tenantId, java.util.UUID organizationId, java.util.UUID agencyId,
            java.util.UUID adminUserId, String reason) {
        return governAgency(tenantId, organizationId, agencyId, adminUserId, reason, AgencyGovernanceAction.SUSPEND);
    }

    @Override
    public Mono<Void> closeAgency(java.util.UUID tenantId, java.util.UUID organizationId, java.util.UUID agencyId,
            java.util.UUID adminUserId, String reason) {
        return governAgency(tenantId, organizationId, agencyId, adminUserId, reason, AgencyGovernanceAction.CLOSE);
    }

    private Mono<Void> governAgency(UUID tenantId, UUID organizationId, UUID agencyId, UUID adminUserId,
            String reason, AgencyGovernanceAction action) {
        return agencyRepository.findById(tenantId, agencyId)
                .switchIfEmpty(Mono.error(new AgencyNotFoundException(agencyId)))
                .filter(agency -> agency.organizationId().equals(organizationId))
                .switchIfEmpty(Mono.error(new AgencyNotFoundException(agencyId)))
                .map(agency -> switch (action) {
                    case ACTIVATE -> agency.activate(adminUserId, reason);
                    case SUSPEND -> agency.suspend(adminUserId, reason);
                    case CLOSE -> agency.close(adminUserId, reason);
                })
                .flatMap(agencyRepository::save)
                .flatMap(saved -> auditAgencyGovernance(saved, adminUserId).then())
                .then();
    }

    private Mono<Void> auditAgencyGovernance(Agency agency, UUID adminUserId) {
        String action = "AGENCY_" + agency.governanceStatus().name();
        String payloadSummary = "status=" + agency.governanceStatus().name()
                + ", reason=" + (agency.governanceReason() == null ? "" : agency.governanceReason());
        return governanceAuditPort
                .map(port -> port.recordAction(agency.tenantId(), agency.organizationId(), adminUserId, action,
                        "AGENCY", agency.id(), payloadSummary))
                .orElse(Mono.empty());
    }

    private Mono<Void> ensureSelfServiceCreationAllowed(java.util.UUID tenantId) {
        return agencySelfServiceCreationPolicy
                .map(policy -> policy.isSelfServiceCreationAllowed(tenantId)
                        .flatMap(isAllowed -> isAllowed
                                ? Mono.<Void>empty()
                                : Mono.error(new AgencySelfServiceCreationDisabledException())))
                .orElseGet(Mono::empty);
    }

    private Mono<Void> validateLogoReference(UUID tenantId, UUID logoId) {
        if (logoId == null) {
            return Mono.empty();
        }
        return storedFileRepository.findById(tenantId, logoId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("logoId does not reference an existing file")))
                .then();
    }

    private enum AgencyGovernanceAction {
        ACTIVATE,
        SUSPEND,
        CLOSE
    }
}
