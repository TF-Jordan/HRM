package yowyob.comops.api.organization.application.service;

import yowyob.comops.api.common.domain.model.PlatformServiceCode;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.organization.application.port.in.CreateOrganizationCommand;
import yowyob.comops.api.organization.application.port.in.CreateOrganizationUseCase;
import yowyob.comops.api.organization.application.port.in.ApproveOrganizationUseCase;
import yowyob.comops.api.organization.application.port.in.CloseOrganizationUseCase;
import yowyob.comops.api.organization.application.port.in.GetOrganizationUseCase;
import yowyob.comops.api.organization.application.port.in.ListMyOrganizationsUseCase;
import yowyob.comops.api.organization.application.port.in.ListOrganizationsUseCase;
import yowyob.comops.api.organization.application.port.in.RejectOrganizationUseCase;
import yowyob.comops.api.organization.application.port.in.ReopenOrganizationUseCase;
import yowyob.comops.api.organization.application.port.in.SearchOrganizationsUseCase;
import yowyob.comops.api.organization.application.port.in.SuspendOrganizationUseCase;
import yowyob.comops.api.organization.application.port.in.TransferOrganizationOwnershipUseCase;
import yowyob.comops.api.organization.application.port.in.UpdateOrganizationCommand;
import yowyob.comops.api.organization.application.port.in.UpdateOrganizationUseCase;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.CurrentBusinessActorProvider;
import yowyob.comops.api.organization.application.port.out.OrganizationApprovalPolicy;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationSearchGateway;
import yowyob.comops.api.organization.application.port.out.OrganizationSelfServiceCreationPolicy;
import yowyob.comops.api.organization.application.port.out.OrganizationServiceSubscriptionRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationGovernanceAuditPort;
import yowyob.comops.api.organization.config.OrganizationServiceSubscriptionQuotaProperties;
import yowyob.comops.api.organization.domain.DuplicateOrganizationCodeException;
import yowyob.comops.api.organization.domain.OrganizationNotFoundException;
import yowyob.comops.api.organization.domain.OrganizationSearchUnavailableException;
import yowyob.comops.api.organization.domain.OrganizationSelfServiceCreationDisabledException;
import yowyob.comops.api.organization.domain.model.Agency;
import yowyob.comops.api.organization.domain.model.Organization;
import yowyob.comops.api.organization.domain.model.OrganizationSearchResult;
import yowyob.comops.api.organization.domain.model.OrganizationServiceSubscription;
import yowyob.comops.api.file.application.port.out.StoredFileRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@Service
public class OrganizationApplicationService
        implements CreateOrganizationUseCase, GetOrganizationUseCase, ListOrganizationsUseCase, SearchOrganizationsUseCase,
        ListMyOrganizationsUseCase, UpdateOrganizationUseCase, TransferOrganizationOwnershipUseCase,
        ApproveOrganizationUseCase, RejectOrganizationUseCase, ReopenOrganizationUseCase,
        SuspendOrganizationUseCase, CloseOrganizationUseCase {

    private final OrganizationRepository organizationRepository;
    private final AgencyRepository agencyRepository;
    private final Optional<OrganizationSearchGateway> organizationSearchGateway;
    private final CurrentBusinessActorProvider currentBusinessActorProvider;
    private final Optional<OrganizationApprovalPolicy> organizationApprovalPolicy;
    private final Optional<OrganizationSelfServiceCreationPolicy> organizationSelfServiceCreationPolicy;
    private final OrganizationServiceSubscriptionRepository organizationServiceSubscriptionRepository;
    private final OrganizationServiceSubscriptionQuotaProperties organizationServiceSubscriptionQuotaProperties;
    private final StoredFileRepository storedFileRepository;
    private final Optional<OrganizationGovernanceAuditPort> governanceAuditPort;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public OrganizationApplicationService(OrganizationRepository organizationRepository,
            AgencyRepository agencyRepository,
            Optional<OrganizationSearchGateway> organizationSearchGateway,
            CurrentBusinessActorProvider currentBusinessActorProvider,
            Optional<OrganizationApprovalPolicy> organizationApprovalPolicy,
            Optional<OrganizationSelfServiceCreationPolicy> organizationSelfServiceCreationPolicy,
            OrganizationServiceSubscriptionRepository organizationServiceSubscriptionRepository,
            OrganizationServiceSubscriptionQuotaProperties organizationServiceSubscriptionQuotaProperties,
            StoredFileRepository storedFileRepository,
            Optional<OrganizationGovernanceAuditPort> governanceAuditPort,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.organizationRepository = organizationRepository;
        this.agencyRepository = agencyRepository;
        this.organizationSearchGateway = organizationSearchGateway;
        this.currentBusinessActorProvider = currentBusinessActorProvider;
        this.organizationApprovalPolicy = organizationApprovalPolicy;
        this.organizationSelfServiceCreationPolicy = organizationSelfServiceCreationPolicy;
        this.organizationServiceSubscriptionRepository = organizationServiceSubscriptionRepository;
        this.organizationServiceSubscriptionQuotaProperties = organizationServiceSubscriptionQuotaProperties;
        this.storedFileRepository = storedFileRepository;
        this.governanceAuditPort = governanceAuditPort;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<Organization> createOrganization(CreateOrganizationCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Organization> operation = ensureSelfServiceCreationAllowed(command.tenantId())
                .then(validateLogoReference(command.tenantId(), command.logoId()))
                .then(applyApprovalPolicy(command.tenantId(), Organization.create(
                        command.tenantId(),
                        command.businessActorId(),
                        command.code(),
                        command.service(),
                        command.isIndividualBusiness(),
                        command.email(),
                        command.shortName(),
                        command.longName(),
                        command.description(),
                        command.logoUri(),
                        command.logoId(),
                        command.websiteUrl(),
                        command.socialNetwork(),
                        command.businessRegistrationNumber(),
                        command.taxNumber(),
                        command.capitalShare(),
                        command.ceoName(),
                        command.yearFounded(),
                        command.keywords(),
                        command.numberOfEmployees(),
                        command.legalForm(),
                        command.isActive(),
                        command.status())))
                .flatMap(organization -> organizationRepository.existsByCode(organization.tenantId(), organization.code())
                        .flatMap(exists -> exists
                                ? Mono.error(new DuplicateOrganizationCodeException(organization.code()))
                                : organizationRepository.save(organization)
                                        .flatMap(saved -> provisionDefaultSubscriptions(saved)
                                                .then(businessEventPublisher.publish(organizationCreatedEvent(saved)))
                                                .thenReturn(saved))));
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<Organization> getOrganization(java.util.UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> organizationRepository.findById(context.tenantId(), organizationId))
                .switchIfEmpty(Mono.error(new OrganizationNotFoundException(organizationId)));
    }

    @Override
    public Flux<Organization> listOrganizations() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> organizationRepository.findByTenantId(context.tenantId()));
    }

    @Override
    public Flux<OrganizationSearchResult> searchOrganizations(String query, String organizationType) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> organizationSearchGateway
                        .map(gateway -> gateway.search(context.tenantId(), query, organizationType))
                        .orElseGet(() -> Flux.error(new OrganizationSearchUnavailableException())));
    }

    @Override
    public Flux<Organization> listMine(java.util.UUID tenantId, java.util.UUID userId) {
        return currentBusinessActorProvider.getCurrentBusinessActorId(tenantId, userId)
                .flatMapMany(businessActorId -> organizationRepository.findByBusinessActorId(tenantId, businessActorId));
    }

    @Override
    public Mono<Organization> update(UpdateOrganizationCommand command) {
        Objects.requireNonNull(command, "command is required");
        Mono<Organization> operation = validateLogoReference(command.tenantId(), command.logoId())
                .then(organizationRepository.findById(command.tenantId(), command.organizationId()))
                .switchIfEmpty(Mono.error(new OrganizationNotFoundException(command.organizationId())))
                .flatMap(existing -> {
                    ensureOwnedBy(existing, command.currentBusinessActorId());
                    return organizationRepository.existsByCodeExcludingId(command.tenantId(), command.code(),
                                    command.organizationId())
                            .flatMap(exists -> exists
                                    ? Mono.error(new DuplicateOrganizationCodeException(command.code()))
                                    : organizationRepository.save(existing.update(command.code(), command.service(),
                                            command.isIndividualBusiness(), command.email(), command.shortName(),
                                            command.longName(), command.description(), command.logoUri(),
                                            command.logoId(), command.websiteUrl(), command.socialNetwork(),
                                            command.businessRegistrationNumber(), command.taxNumber(),
                                            command.capitalShare(), command.ceoName(), command.yearFounded(),
                                            command.keywords(), command.numberOfEmployees(), command.legalForm(),
                                            command.isActive(), command.status())));
                });
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<Organization> transfer(java.util.UUID tenantId, java.util.UUID organizationId,
            java.util.UUID currentBusinessActorId, java.util.UUID newBusinessActorId) {
        Mono<Organization> operation = organizationRepository.findById(tenantId, organizationId)
                .switchIfEmpty(Mono.error(new OrganizationNotFoundException(organizationId)))
                .map(existing -> {
                    ensureOwnedBy(existing, currentBusinessActorId);
                    return existing.transferOwnership(newBusinessActorId);
                })
                .flatMap(organizationRepository::save);
        return transactionalExecutor.transactional(operation);
    }

    @Override
    public Mono<Void> approveOrganization(java.util.UUID tenantId, java.util.UUID organizationId,
            java.util.UUID adminUserId, String reason) {
        return governOrganization(tenantId, organizationId, adminUserId, reason, GovernanceAction.APPROVE, false);
    }

    @Override
    public Mono<Void> rejectOrganization(java.util.UUID tenantId, java.util.UUID organizationId,
            java.util.UUID adminUserId, String reason) {
        return governOrganization(tenantId, organizationId, adminUserId, reason, GovernanceAction.REJECT, false);
    }

    @Override
    public Mono<Void> reopenOrganization(java.util.UUID tenantId, java.util.UUID organizationId,
            java.util.UUID adminUserId, String reason) {
        return governOrganization(tenantId, organizationId, adminUserId, reason, GovernanceAction.REOPEN, false);
    }

    @Override
    public Mono<Void> suspendOrganization(java.util.UUID tenantId, java.util.UUID organizationId,
            java.util.UUID adminUserId, String reason) {
        return governOrganization(tenantId, organizationId, adminUserId, reason, GovernanceAction.SUSPEND, true);
    }

    @Override
    public Mono<Void> closeOrganization(java.util.UUID tenantId, java.util.UUID organizationId,
            java.util.UUID adminUserId, String reason) {
        return governOrganization(tenantId, organizationId, adminUserId, reason, GovernanceAction.CLOSE, true);
    }

    private Mono<Void> governOrganization(UUID tenantId, UUID organizationId, UUID adminUserId, String reason,
            GovernanceAction action, boolean cascadeToAgencies) {
        Mono<Void> operation = organizationRepository.findById(tenantId, organizationId)
                .switchIfEmpty(Mono.error(new OrganizationNotFoundException(organizationId)))
                .map(organization -> applyGovernanceAction(organization, action, adminUserId, reason))
                .flatMap(organizationRepository::save)
                .flatMap(saved -> auditOrganizationGovernance(saved, adminUserId)
                        .then(cascadeToAgencies
                                ? cascadeAgencyGovernance(tenantId, organizationId, adminUserId, reason, action)
                                : Mono.empty()))
                .then();
        return transactionalExecutor.transactional(operation);
    }

    private Mono<Void> cascadeAgencyGovernance(UUID tenantId, UUID organizationId, UUID adminUserId,
            String reason, GovernanceAction action) {
        return agencyRepository.findByOrganizationId(tenantId, organizationId)
                .flatMap(agency -> agencyRepository.save(switch (action) {
                    case SUSPEND -> agency.suspend(adminUserId, cascadeReason(reason, "organization suspension"));
                    case CLOSE -> agency.close(adminUserId, cascadeReason(reason, "organization closure"));
                    default -> agency;
                }).flatMap(saved -> auditAgencyGovernance(saved, adminUserId).thenReturn(saved)))
                .then();
    }

    private String cascadeReason(String reason, String fallback) {
        return reason == null || reason.isBlank() ? "Cascaded from " + fallback : reason;
    }

    private Organization applyGovernanceAction(Organization organization, GovernanceAction action, UUID adminUserId,
            String reason) {
        return switch (action) {
            case APPROVE -> organization.approve(adminUserId, reason);
            case REJECT -> organization.reject(adminUserId, reason);
            case REOPEN -> organization.reopen(adminUserId, reason);
            case SUSPEND -> organization.suspend(adminUserId, reason);
            case CLOSE -> organization.close(adminUserId, reason);
            case ACTIVATE -> throw new IllegalArgumentException("Unsupported organization governance action");
        };
    }

    private Mono<Void> auditOrganizationGovernance(Organization organization, UUID adminUserId) {
        String action = "ORGANIZATION_" + organization.governanceStatus().name();
        String payloadSummary = "status=" + organization.governanceStatus().name()
                + ", reason=" + (organization.governanceReason() == null ? "" : organization.governanceReason());
        return governanceAuditPort
                .map(port -> port.recordAction(organization.tenantId(), organization.id(), adminUserId, action,
                        "ORGANIZATION", organization.id(), payloadSummary))
                .orElse(Mono.empty());
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

    private void ensureOwnedBy(Organization organization, java.util.UUID businessActorId) {
        if (!organization.businessActorId().equals(businessActorId)) {
            throw new IllegalStateException("organization is not owned by the current business actor");
        }
    }

    private BusinessEvent organizationCreatedEvent(Organization organization) {
        return BusinessEvent.now(organization.tenantId(), organization.id(), "ORGANIZATION_CREATED", "ORGANIZATION",
                organization.id(), payload(
                        "code", organization.code(),
                        "service", organization.service(),
                        "shortName", organization.shortName(),
                        "longName", organization.longName(),
                        "legalForm", organization.legalForm(),
                        "isActive", organization.isActive(),
                        "status", organization.status(),
                        "legalName", organization.legalName(),
                        "displayName", organization.displayName(),
                        "organizationType", organization.organizationType(),
                        "governanceStatus", organization.governanceStatus().name(),
                        "businessActorId", organization.businessActorId()));
    }

    private Mono<Void> provisionDefaultSubscriptions(Organization organization) {
        return reactor.core.publisher.Flux.fromIterable(PlatformServiceCode.subscribableCodes())
                .concatMap(serviceCode -> organizationServiceSubscriptionRepository
                        .existsByOrganizationAndServiceCode(organization.tenantId(), organization.id(), serviceCode)
                        .flatMap(exists -> exists
                                ? Mono.empty()
                                : organizationServiceSubscriptionRepository.save(
                                                OrganizationServiceSubscription.create(organization.tenantId(),
                                                        organization.id(), serviceCode,
                                                        organizationServiceSubscriptionQuotaProperties.getDefaultRequestQuotaLimit(),
                                                        Math.max(1L, organizationServiceSubscriptionQuotaProperties
                                                                .getDefaultRequestQuotaWindow().toSeconds())))
                                        .then()))
                .then();
    }

    private Mono<Organization> applyApprovalPolicy(java.util.UUID tenantId, Organization organization) {
        return organizationApprovalPolicy
                .map(policy -> policy.requiresApproval(tenantId)
                        .map(requiresApproval -> requiresApproval ? organization : organization.approve(null, "auto-approved by platform options")))
                .orElseGet(() -> Mono.just(organization));
    }

    private Mono<Void> ensureSelfServiceCreationAllowed(java.util.UUID tenantId) {
        return organizationSelfServiceCreationPolicy
                .map(policy -> policy.isSelfServiceCreationAllowed(tenantId)
                        .flatMap(isAllowed -> isAllowed
                                ? Mono.<Void>empty()
                                : Mono.error(new OrganizationSelfServiceCreationDisabledException())))
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

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }

    private enum GovernanceAction {
        APPROVE,
        REJECT,
        REOPEN,
        SUSPEND,
        CLOSE,
        ACTIVATE
    }
}
