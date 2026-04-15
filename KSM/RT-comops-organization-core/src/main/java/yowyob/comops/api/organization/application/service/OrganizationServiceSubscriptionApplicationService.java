package yowyob.comops.api.organization.application.service;

import yowyob.comops.api.common.domain.model.PlatformServiceCode;
import yowyob.comops.api.organization.application.port.in.CheckOrganizationServiceEntitlementUseCase;
import yowyob.comops.api.organization.application.port.in.GetOrganizationServiceEntitlementsUseCase;
import yowyob.comops.api.organization.application.port.in.ListPlatformServicesUseCase;
import yowyob.comops.api.organization.application.port.in.ListUserOrganizationAccessUseCase;
import yowyob.comops.api.organization.application.port.in.OrganizationServiceCatalogEntry;
import yowyob.comops.api.organization.application.port.in.OrganizationServiceEntitlements;
import yowyob.comops.api.organization.application.port.in.OrganizationServiceQuota;
import yowyob.comops.api.organization.application.port.in.OrganizationServiceRuntimePolicy;
import yowyob.comops.api.organization.application.port.in.ResolveOrganizationServiceRuntimePolicyUseCase;
import yowyob.comops.api.organization.application.port.in.SubscribeOrganizationServiceUseCase;
import yowyob.comops.api.organization.application.port.in.UnsubscribeOrganizationServiceUseCase;
import yowyob.comops.api.organization.application.port.in.UpdateOrganizationServiceQuotaUseCase;
import yowyob.comops.api.organization.application.port.in.UserOrganizationAccessView;
import yowyob.comops.api.organization.application.port.out.CurrentBusinessActorProvider;
import yowyob.comops.api.organization.application.port.out.EmployeeMembershipRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationServiceSubscriptionRepository;
import yowyob.comops.api.organization.config.OrganizationServiceSubscriptionQuotaProperties;
import yowyob.comops.api.organization.domain.OrganizationNotFoundException;
import yowyob.comops.api.organization.domain.model.Organization;
import yowyob.comops.api.organization.domain.model.OrganizationServiceSubscription;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrganizationServiceSubscriptionApplicationService
        implements ListPlatformServicesUseCase, GetOrganizationServiceEntitlementsUseCase,
        SubscribeOrganizationServiceUseCase, UnsubscribeOrganizationServiceUseCase,
        CheckOrganizationServiceEntitlementUseCase, ListUserOrganizationAccessUseCase,
        ResolveOrganizationServiceRuntimePolicyUseCase, UpdateOrganizationServiceQuotaUseCase {

    private final OrganizationRepository organizationRepository;
    private final OrganizationServiceSubscriptionRepository subscriptionRepository;
    private final EmployeeMembershipRepository employeeMembershipRepository;
    private final CurrentBusinessActorProvider currentBusinessActorProvider;
    private final OrganizationServiceSubscriptionQuotaProperties quotaProperties;

    public OrganizationServiceSubscriptionApplicationService(OrganizationRepository organizationRepository,
            OrganizationServiceSubscriptionRepository subscriptionRepository,
            EmployeeMembershipRepository employeeMembershipRepository,
            CurrentBusinessActorProvider currentBusinessActorProvider,
            OrganizationServiceSubscriptionQuotaProperties quotaProperties) {
        this.organizationRepository = organizationRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.employeeMembershipRepository = employeeMembershipRepository;
        this.currentBusinessActorProvider = currentBusinessActorProvider;
        this.quotaProperties = quotaProperties;
    }

    @Override
    public Flux<OrganizationServiceCatalogEntry> listPlatformServices() {
        return Flux.fromIterable(PlatformServiceCode.catalog())
                .map(this::toCatalogEntry);
    }

    @Override
    public Mono<OrganizationServiceEntitlements> getOrganizationServiceEntitlements(UUID tenantId, UUID organizationId) {
        return requireOrganization(tenantId, organizationId)
                .then(buildEntitlements(tenantId, organizationId));
    }

    @Override
    public Mono<OrganizationServiceEntitlements> subscribeOrganizationService(UUID tenantId, UUID organizationId,
            String serviceCode, Long requestQuotaLimit, Long requestQuotaWindowSeconds) {
        PlatformServiceCode service = requireSubscribable(serviceCode);
        long resolvedQuotaLimit = resolveQuotaLimit(requestQuotaLimit);
        long resolvedQuotaWindowSeconds = resolveQuotaWindowSeconds(requestQuotaWindowSeconds);
        return requireOrganization(tenantId, organizationId)
                .then(subscriptionRepository.existsByOrganizationAndServiceCode(tenantId, organizationId, service.code()))
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : subscriptionRepository.save(
                                OrganizationServiceSubscription.create(tenantId, organizationId, service.code(),
                                        resolvedQuotaLimit, resolvedQuotaWindowSeconds))
                                .then())
                .then(buildEntitlements(tenantId, organizationId));
    }

    @Override
    public Mono<OrganizationServiceEntitlements> unsubscribeOrganizationService(UUID tenantId, UUID organizationId,
            String serviceCode) {
        PlatformServiceCode service = requireSubscribable(serviceCode);
        return requireOrganization(tenantId, organizationId)
                .then(subscriptionRepository.deleteByOrganizationAndServiceCode(tenantId, organizationId, service.code()))
                .then(buildEntitlements(tenantId, organizationId));
    }

    @Override
    public Mono<Boolean> hasOrganizationService(UUID tenantId, UUID organizationId, String serviceCode) {
        PlatformServiceCode service = PlatformServiceCode.from(serviceCode);
        return organizationRepository.findById(tenantId, organizationId)
                .flatMap(organization -> service.mandatory()
                        ? Mono.just(Boolean.TRUE)
                        : subscriptionRepository.existsByOrganizationAndServiceCode(tenantId, organizationId, service.code()))
                .defaultIfEmpty(Boolean.FALSE);
    }

    @Override
    public Mono<OrganizationServiceRuntimePolicy> resolveOrganizationServiceRuntimePolicy(UUID tenantId, UUID organizationId,
            String serviceCode) {
        PlatformServiceCode service = PlatformServiceCode.from(serviceCode);
        return organizationRepository.findById(tenantId, organizationId)
                .flatMap(organization -> {
                    if (service.mandatory()) {
                        return Mono.just(new OrganizationServiceRuntimePolicy(service.code(), true, null, null));
                    }
                    return subscriptionRepository.findByOrganizationAndServiceCode(tenantId, organizationId, service.code())
                            .map(subscription -> new OrganizationServiceRuntimePolicy(
                                    subscription.serviceCode(),
                                    true,
                                    subscription.requestQuotaLimit(),
                                    subscription.requestQuotaWindowSeconds()))
                            .switchIfEmpty(Mono.just(new OrganizationServiceRuntimePolicy(service.code(), false, null, null)));
                })
                .switchIfEmpty(Mono.just(new OrganizationServiceRuntimePolicy(service.code(), false, null, null)));
    }

    @Override
    public Mono<OrganizationServiceEntitlements> updateOrganizationServiceQuota(UUID tenantId, UUID organizationId,
            String serviceCode, Long requestQuotaLimit, Long requestQuotaWindowSeconds) {
        PlatformServiceCode service = requireSubscribable(serviceCode);
        long resolvedQuotaLimit = resolveQuotaLimit(requestQuotaLimit);
        long resolvedQuotaWindowSeconds = resolveQuotaWindowSeconds(requestQuotaWindowSeconds);
        return requireOrganization(tenantId, organizationId)
                .then(subscriptionRepository.findByOrganizationAndServiceCode(tenantId, organizationId, service.code()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "organization is not subscribed to service " + service.code())))
                .flatMap(subscription -> subscriptionRepository.save(
                        subscription.updateQuota(resolvedQuotaLimit, resolvedQuotaWindowSeconds)))
                .then(buildEntitlements(tenantId, organizationId));
    }

    @Override
    public Flux<UserOrganizationAccessView> listUserOrganizationAccess(UUID tenantId, UUID userId) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(userId, "userId is required");
        Flux<Organization> ownedOrganizations = currentBusinessActorProvider.getCurrentBusinessActorId(tenantId, userId)
                .onErrorResume(exception -> Mono.empty())
                .flatMapMany(businessActorId -> organizationRepository.findByBusinessActorId(tenantId, businessActorId));
        Flux<Organization> memberOrganizations = employeeMembershipRepository.findByUserId(tenantId, userId)
                .flatMap(membership -> organizationRepository.findById(tenantId, membership.organizationId()));
        return Flux.merge(ownedOrganizations, memberOrganizations)
                .distinct(Organization::id)
                .flatMap(organization -> buildEntitlements(tenantId, organization.id())
                        .map(entitlements -> new UserOrganizationAccessView(
                                organization.id(),
                                organization.code(),
                                organization.shortName(),
                                organization.longName(),
                                entitlements.effectiveServices())));
    }

    private Mono<Void> requireOrganization(UUID tenantId, UUID organizationId) {
        return organizationRepository.findById(tenantId, organizationId)
                .switchIfEmpty(Mono.error(new OrganizationNotFoundException(organizationId)))
                .then();
    }

    private Mono<OrganizationServiceEntitlements> buildEntitlements(UUID tenantId, UUID organizationId) {
        return subscriptionRepository.findByOrganizationId(tenantId, organizationId)
                .collectList()
                .map(subscriptions -> {
                    Set<String> subscribedCodes = new LinkedHashSet<>(subscriptions.stream()
                            .map(OrganizationServiceSubscription::serviceCode)
                            .toList());
                    List<String> orderedSubscribedCodes = PlatformServiceCode.orderCodes(subscribedCodes);
                    Set<String> effectiveCodes = new LinkedHashSet<>(PlatformServiceCode.mandatoryCodes());
                    effectiveCodes.addAll(orderedSubscribedCodes);
                    List<OrganizationServiceQuota> quotas = orderedSubscribedCodes.stream()
                            .map(serviceCode -> subscriptions.stream()
                                    .filter(subscription -> subscription.serviceCode().equals(serviceCode))
                                    .findFirst()
                                    .map(subscription -> new OrganizationServiceQuota(subscription.serviceCode(),
                                            subscription.requestQuotaLimit(),
                                            subscription.requestQuotaWindowSeconds()))
                                    .orElse(null))
                            .filter(Objects::nonNull)
                            .toList();
                    return new OrganizationServiceEntitlements(
                            organizationId,
                            orderedSubscribedCodes,
                            PlatformServiceCode.orderCodes(effectiveCodes),
                            quotas);
                });
    }

    private PlatformServiceCode requireSubscribable(String serviceCode) {
        PlatformServiceCode service = PlatformServiceCode.from(serviceCode);
        if (!service.subscribable()) {
            throw new IllegalArgumentException(service.code() + " is a mandatory platform service and cannot be managed as a subscription");
        }
        return service;
    }

    private long resolveQuotaLimit(Long requestedQuotaLimit) {
        long resolved = requestedQuotaLimit == null ? quotaProperties.getDefaultRequestQuotaLimit() : requestedQuotaLimit;
        if (resolved <= 0) {
            throw new IllegalArgumentException("requestQuotaLimit must be > 0");
        }
        return resolved;
    }

    private long resolveQuotaWindowSeconds(Long requestedQuotaWindowSeconds) {
        long resolved = requestedQuotaWindowSeconds == null
                ? Math.max(1L, quotaProperties.getDefaultRequestQuotaWindow().toSeconds())
                : requestedQuotaWindowSeconds;
        if (resolved <= 0) {
            throw new IllegalArgumentException("requestQuotaWindowSeconds must be > 0");
        }
        return resolved;
    }

    private OrganizationServiceCatalogEntry toCatalogEntry(PlatformServiceCode service) {
        return new OrganizationServiceCatalogEntry(service.code(), service.displayName(), service.description(),
                service.mandatory(), service.subscribable());
    }
}
