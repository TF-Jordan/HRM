package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OrganizationServiceSubscriptionRepository;
import yowyob.comops.api.organization.domain.model.OrganizationServiceSubscription;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class OrganizationServiceSubscriptionR2dbcRepositoryAdapter implements OrganizationServiceSubscriptionRepository {

    private final OrganizationServiceSubscriptionSpringDataRepository repository;

    public OrganizationServiceSubscriptionR2dbcRepositoryAdapter(
            OrganizationServiceSubscriptionSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByOrganizationAndServiceCode(UUID tenantId, UUID organizationId, String serviceCode) {
        return repository.existsByTenantIdAndOrganizationIdAndServiceCode(tenantId, organizationId, serviceCode);
    }

    @Override
    public Mono<OrganizationServiceSubscription> findByOrganizationAndServiceCode(UUID tenantId, UUID organizationId,
            String serviceCode) {
        return repository.findByTenantIdAndOrganizationIdAndServiceCode(tenantId, organizationId, serviceCode)
                .map(this::toDomain);
    }

    @Override
    public Flux<OrganizationServiceSubscription> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Mono<OrganizationServiceSubscription> save(OrganizationServiceSubscription subscription) {
        return repository.save(toEntity(subscription))
                .map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteByOrganizationAndServiceCode(UUID tenantId, UUID organizationId, String serviceCode) {
        return repository.deleteByTenantIdAndOrganizationIdAndServiceCode(tenantId, organizationId, serviceCode);
    }

    private OrganizationServiceSubscriptionEntity toEntity(OrganizationServiceSubscription subscription) {
        return new OrganizationServiceSubscriptionEntity(
                subscription.id(),
                subscription.tenantId(),
                subscription.createdAt(),
                subscription.updatedAt(),
                subscription.organizationId(),
                subscription.serviceCode(),
                subscription.requestQuotaLimit(),
                subscription.requestQuotaWindowSeconds());
    }

    private OrganizationServiceSubscription toDomain(OrganizationServiceSubscriptionEntity entity) {
        return OrganizationServiceSubscription.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(),
                entity.updatedAt(), entity.organizationId(), entity.serviceCode(),
                entity.requestQuotaLimit() == null ? 0L : entity.requestQuotaLimit(),
                entity.requestQuotaWindowSeconds() == null ? 0L : entity.requestQuotaWindowSeconds());
    }
}
