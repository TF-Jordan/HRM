package yowyob.comops.api.organization.application.service;

import yowyob.comops.api.common.domain.model.PlatformCommercialAddOnCode;
import yowyob.comops.api.common.domain.model.PlatformCommercialPlanCode;
import yowyob.comops.api.common.domain.model.PlatformServiceCode;
import yowyob.comops.api.common.domain.model.PlatformServicePackCode;
import yowyob.comops.api.organization.application.port.in.ApplyOrganizationCommercialSubscriptionUseCase;
import yowyob.comops.api.organization.application.port.in.CheckOrganizationServiceEntitlementUseCase;
import yowyob.comops.api.organization.application.port.in.CommercialAddOnCatalogEntry;
import yowyob.comops.api.organization.application.port.in.CommercialPlanCatalogEntry;
import yowyob.comops.api.organization.application.port.in.CommercialServiceQuota;
import yowyob.comops.api.organization.application.port.in.CommercialSubscriptionCatalog;
import yowyob.comops.api.organization.application.port.in.GetOrganizationServiceEntitlementsUseCase;
import yowyob.comops.api.organization.application.port.in.ListCommercialSubscriptionCatalogUseCase;
import yowyob.comops.api.organization.application.port.in.ListPlatformServicesUseCase;
import yowyob.comops.api.organization.application.port.in.ListPlatformServicePacksUseCase;
import yowyob.comops.api.organization.application.port.in.OrganizationServiceDependencyIssue;
import yowyob.comops.api.organization.application.port.in.ListUserOrganizationAccessUseCase;
import yowyob.comops.api.organization.application.port.in.OrganizationServiceCatalogEntry;
import yowyob.comops.api.organization.application.port.in.OrganizationServiceEntitlements;
import yowyob.comops.api.organization.application.port.in.OrganizationServicePackEntry;
import yowyob.comops.api.organization.application.port.in.OrganizationServiceQuota;
import yowyob.comops.api.organization.application.port.in.OrganizationServiceRuntimePolicy;
import yowyob.comops.api.organization.application.port.in.OrganizationCommercialSubscription;
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
import java.util.LinkedHashMap;
import java.util.Map;
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
        ResolveOrganizationServiceRuntimePolicyUseCase, UpdateOrganizationServiceQuotaUseCase,
        ListPlatformServicePacksUseCase, ListCommercialSubscriptionCatalogUseCase,
        ApplyOrganizationCommercialSubscriptionUseCase {

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
    public Flux<OrganizationServicePackEntry> listPlatformServicePacks() {
        return Flux.fromIterable(PlatformServicePackCode.catalog())
                .map(pack -> new OrganizationServicePackEntry(pack.code(), pack.displayName(), pack.description(),
                        pack.serviceCodes()));
    }

    @Override
    public Mono<CommercialSubscriptionCatalog> listCommercialSubscriptionCatalog() {
        return Mono.fromSupplier(() -> new CommercialSubscriptionCatalog(
                PlatformCommercialPlanCode.catalog().stream()
                        .map(this::toCommercialPlanEntry)
                        .toList(),
                PlatformCommercialAddOnCode.catalog().stream()
                        .map(this::toCommercialAddOnEntry)
                        .toList()));
    }

    @Override
    public Mono<OrganizationCommercialSubscription> applyOrganizationCommercialSubscription(UUID tenantId,
            UUID organizationId, String planCode, List<String> addOnCodes) {
        PlatformCommercialPlanCode plan = PlatformCommercialPlanCode.from(planCode);
        List<PlatformCommercialAddOnCode> addOns = normalizeAddOns(plan, addOnCodes);
        LinkedHashSet<String> targetServiceCodes = commercialServiceCodes(plan, addOns);
        validateCommercialServiceSet(targetServiceCodes);
        Map<String, CommercialServiceQuota> quotasByService = commercialQuotas(plan, addOns, targetServiceCodes);

        return requireOrganization(tenantId, organizationId)
                .then(subscriptionRepository.findByOrganizationId(tenantId, organizationId).collectList())
                .flatMap(existingSubscriptions -> {
                    Map<String, OrganizationServiceSubscription> existingByService = new LinkedHashMap<>();
                    existingSubscriptions.forEach(subscription -> existingByService.put(subscription.serviceCode(),
                            subscription));
                    Mono<Void> upserts = Flux.fromIterable(targetServiceCodes)
                            .concatMap(serviceCode -> {
                                CommercialServiceQuota quota = quotasByService.get(serviceCode);
                                OrganizationServiceSubscription existing = existingByService.get(serviceCode);
                                if (existing == null) {
                                    return subscriptionRepository.save(OrganizationServiceSubscription.create(
                                            tenantId, organizationId, serviceCode, quota.requestQuotaLimit(),
                                            quota.requestQuotaWindowSeconds()));
                                }
                                return subscriptionRepository.save(existing.updateQuota(quota.requestQuotaLimit(),
                                        quota.requestQuotaWindowSeconds()));
                            })
                            .then();
                    Mono<Void> deletes = Flux.fromIterable(existingSubscriptions)
                            .filter(subscription -> !targetServiceCodes.contains(subscription.serviceCode()))
                            .concatMap(subscription -> subscriptionRepository.deleteByOrganizationAndServiceCode(
                                    tenantId, organizationId, subscription.serviceCode()))
                            .then();
                    return upserts.then(deletes);
                })
                .then(Mono.defer(() -> buildEntitlements(tenantId, organizationId)))
                .map(entitlements -> new OrganizationCommercialSubscription(
                        organizationId,
                        plan.code(),
                        addOns.stream().map(PlatformCommercialAddOnCode::code).toList(),
                        PlatformServiceCode.orderCodes(targetServiceCodes),
                        quotasByService.values().stream().toList(),
                        entitlements.dependencyIssues(),
                        entitlements));
    }

    @Override
    public Mono<OrganizationServiceEntitlements> getOrganizationServiceEntitlements(UUID tenantId, UUID organizationId) {
        return requireOrganization(tenantId, organizationId)
                .then(Mono.defer(() -> buildEntitlements(tenantId, organizationId)));
    }

    @Override
    public Mono<OrganizationServiceEntitlements> subscribeOrganizationService(UUID tenantId, UUID organizationId,
            String serviceCode, Long requestQuotaLimit, Long requestQuotaWindowSeconds) {
        PlatformServiceCode service = requireSubscribable(serviceCode);
        long resolvedQuotaLimit = resolveQuotaLimit(requestQuotaLimit);
        long resolvedQuotaWindowSeconds = resolveQuotaWindowSeconds(requestQuotaWindowSeconds);
        return requireOrganization(tenantId, organizationId)
                .then(loadSubscribedServiceCodes(tenantId, organizationId)
                        .flatMap(subscribedCodes -> {
                            validateRequiredDependenciesPresent(service, subscribedCodes);
                            if (subscribedCodes.contains(service.code())) {
                                return Mono.empty();
                            }
                            return subscriptionRepository.save(
                                    OrganizationServiceSubscription.create(tenantId, organizationId, service.code(),
                                            resolvedQuotaLimit, resolvedQuotaWindowSeconds))
                                    .then();
                        }))
                .then(Mono.defer(() -> buildEntitlements(tenantId, organizationId)));
    }

    @Override
    public Mono<OrganizationServiceEntitlements> unsubscribeOrganizationService(UUID tenantId, UUID organizationId,
            String serviceCode) {
        PlatformServiceCode service = requireSubscribable(serviceCode);
        return requireOrganization(tenantId, organizationId)
                .then(loadSubscribedServiceCodes(tenantId, organizationId)
                        .flatMap(subscribedCodes -> {
                            subscribedCodes.remove(service.code());
                            validateDependentsRemainCoherent(service, subscribedCodes);
                            return subscriptionRepository.deleteByOrganizationAndServiceCode(tenantId, organizationId,
                                    service.code());
                        }))
                .then(Mono.defer(() -> buildEntitlements(tenantId, organizationId)));
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
                .then(Mono.defer(() -> buildEntitlements(tenantId, organizationId)));
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
                    List<OrganizationServiceDependencyIssue> dependencyIssues = orderedSubscribedCodes.stream()
                            .map(serviceCode -> dependencyIssueFor(serviceCode, effectiveCodes))
                            .filter(Objects::nonNull)
                            .toList();
                    return new OrganizationServiceEntitlements(
                            organizationId,
                            orderedSubscribedCodes,
                            PlatformServiceCode.orderCodes(effectiveCodes),
                            quotas,
                            dependencyIssues);
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
                service.mandatory(), service.subscribable(), service.requiredDependencyCodes(),
                service.recommendedDependencyCodes(), PlatformServicePackCode.packCodesForService(service.code()));
    }

    private CommercialPlanCatalogEntry toCommercialPlanEntry(PlatformCommercialPlanCode plan) {
        return new CommercialPlanCatalogEntry(
                plan.code(),
                plan.displayName(),
                plan.description(),
                plan.packCodes(),
                plan.serviceCodes(),
                plan.compatibleAddOnCodes(),
                plan.serviceQuotas().entrySet().stream()
                        .map(entry -> new CommercialServiceQuota(entry.getKey(),
                                entry.getValue().requestQuotaLimit(),
                                entry.getValue().requestQuotaWindowSeconds()))
                        .toList());
    }

    private CommercialAddOnCatalogEntry toCommercialAddOnEntry(PlatformCommercialAddOnCode addOn) {
        return new CommercialAddOnCatalogEntry(
                addOn.code(),
                addOn.displayName(),
                addOn.description(),
                addOn.serviceCodes(),
                addOn.compatiblePlanCodes(),
                addOn.serviceQuotas().entrySet().stream()
                        .map(entry -> new CommercialServiceQuota(entry.getKey(),
                                entry.getValue().requestQuotaLimit(),
                                entry.getValue().requestQuotaWindowSeconds()))
                        .toList());
    }

    private Mono<Set<String>> loadSubscribedServiceCodes(UUID tenantId, UUID organizationId) {
        return subscriptionRepository.findByOrganizationId(tenantId, organizationId)
                .map(OrganizationServiceSubscription::serviceCode)
                .collectList()
                .map(LinkedHashSet::new);
    }

    private void validateRequiredDependenciesPresent(PlatformServiceCode service, Set<String> subscribedCodes) {
        List<String> missingRequiredServices = service.requiredDependencyCodes().stream()
                .filter(dependency -> !subscribedCodes.contains(dependency))
                .toList();
        if (!missingRequiredServices.isEmpty()) {
            throw new IllegalArgumentException("cannot subscribe " + service.code()
                    + " without required services " + String.join(", ", missingRequiredServices));
        }
    }

    private List<PlatformCommercialAddOnCode> normalizeAddOns(PlatformCommercialPlanCode plan, List<String> addOnCodes) {
        if (addOnCodes == null || addOnCodes.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> normalizedCodes = new LinkedHashSet<>();
        for (String addOnCode : addOnCodes) {
            PlatformCommercialAddOnCode addOn = PlatformCommercialAddOnCode.from(addOnCode);
            if (!addOn.compatiblePlanCodes().contains(plan.code())) {
                throw new IllegalArgumentException("add-on " + addOn.code() + " is not compatible with plan "
                        + plan.code());
            }
            normalizedCodes.add(addOn.code());
        }
        return PlatformCommercialAddOnCode.catalog().stream()
                .filter(addOn -> normalizedCodes.contains(addOn.code()))
                .toList();
    }

    private LinkedHashSet<String> commercialServiceCodes(PlatformCommercialPlanCode plan,
            List<PlatformCommercialAddOnCode> addOns) {
        LinkedHashSet<String> serviceCodes = new LinkedHashSet<>(plan.serviceCodes());
        addOns.stream()
                .flatMap(addOn -> addOn.serviceCodes().stream())
                .forEach(serviceCodes::add);
        return new LinkedHashSet<>(PlatformServiceCode.orderCodes(serviceCodes));
    }

    private void validateCommercialServiceSet(Set<String> serviceCodes) {
        for (String serviceCode : serviceCodes) {
            PlatformServiceCode service = requireSubscribable(serviceCode);
            List<String> missingRequiredServices = service.requiredDependencyCodes().stream()
                    .filter(dependency -> !serviceCodes.contains(dependency))
                    .toList();
            if (!missingRequiredServices.isEmpty()) {
                throw new IllegalArgumentException("commercial package service " + service.code()
                        + " misses required services " + String.join(", ", missingRequiredServices));
            }
        }
    }

    private Map<String, CommercialServiceQuota> commercialQuotas(PlatformCommercialPlanCode plan,
            List<PlatformCommercialAddOnCode> addOns, Set<String> serviceCodes) {
        Map<String, CommercialServiceQuota> quotasByService = new LinkedHashMap<>();
        serviceCodes.forEach(serviceCode -> quotasByService.put(serviceCode, new CommercialServiceQuota(
                serviceCode, quotaProperties.getDefaultRequestQuotaLimit(),
                Math.max(1L, quotaProperties.getDefaultRequestQuotaWindow().toSeconds()))));
        plan.serviceQuotas().forEach((serviceCode, quota) -> quotasByService.put(serviceCode,
                new CommercialServiceQuota(serviceCode, quota.requestQuotaLimit(), quota.requestQuotaWindowSeconds())));
        addOns.forEach(addOn -> addOn.serviceQuotas().forEach((serviceCode, quota) -> quotasByService.put(serviceCode,
                new CommercialServiceQuota(serviceCode, quota.requestQuotaLimit(), quota.requestQuotaWindowSeconds()))));
        return quotasByService;
    }

    private void validateDependentsRemainCoherent(PlatformServiceCode removedService, Set<String> subscribedCodes) {
        List<String> impactedServices = PlatformServiceCode.subscribableCodes().stream()
                .filter(subscribedCodes::contains)
                .filter(serviceCode -> PlatformServiceCode.from(serviceCode).requiredDependencyCodes().contains(removedService.code()))
                .toList();
        if (!impactedServices.isEmpty()) {
            throw new IllegalArgumentException("cannot unsubscribe " + removedService.code()
                    + " while subscribed services depend on it: " + String.join(", ", impactedServices));
        }
    }

    private OrganizationServiceDependencyIssue dependencyIssueFor(String serviceCode, Set<String> effectiveCodes) {
        PlatformServiceCode service = PlatformServiceCode.from(serviceCode);
        List<String> missingRequiredServices = service.requiredDependencyCodes().stream()
                .filter(dependency -> !effectiveCodes.contains(dependency))
                .toList();
        List<String> missingRecommendedServices = service.recommendedDependencyCodes().stream()
                .filter(dependency -> !effectiveCodes.contains(dependency))
                .toList();
        if (missingRequiredServices.isEmpty() && missingRecommendedServices.isEmpty()) {
            return null;
        }
        return new OrganizationServiceDependencyIssue(service.code(), missingRequiredServices, missingRecommendedServices);
    }
}
