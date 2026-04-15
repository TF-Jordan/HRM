package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OrganizationServiceSubscriptionRepository;
import yowyob.comops.api.organization.domain.model.OrganizationServiceSubscription;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryOrganizationServiceSubscriptionRepository implements OrganizationServiceSubscriptionRepository {

    private final Map<UUID, OrganizationServiceSubscription> subscriptions = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByOrganizationAndServiceCode(UUID tenantId, UUID organizationId, String serviceCode) {
        return Mono.fromSupplier(() -> subscriptions.values().stream()
                .filter(subscription -> subscription.tenantId().equals(tenantId))
                .filter(subscription -> subscription.organizationId().equals(organizationId))
                .anyMatch(subscription -> subscription.serviceCode().equals(serviceCode)));
    }

    @Override
    public Mono<OrganizationServiceSubscription> findByOrganizationAndServiceCode(UUID tenantId, UUID organizationId,
            String serviceCode) {
        return Mono.justOrEmpty(subscriptions.values().stream()
                .filter(subscription -> subscription.tenantId().equals(tenantId))
                .filter(subscription -> subscription.organizationId().equals(organizationId))
                .filter(subscription -> subscription.serviceCode().equals(serviceCode))
                .findFirst());
    }

    @Override
    public Flux<OrganizationServiceSubscription> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(subscriptions.values().stream()
                .filter(subscription -> subscription.tenantId().equals(tenantId))
                .filter(subscription -> subscription.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<OrganizationServiceSubscription> save(OrganizationServiceSubscription subscription) {
        return Mono.fromSupplier(() -> {
            subscriptions.put(subscription.id(), subscription);
            return subscription;
        });
    }

    @Override
    public Mono<Void> deleteByOrganizationAndServiceCode(UUID tenantId, UUID organizationId, String serviceCode) {
        return Mono.fromRunnable(() -> subscriptions.entrySet().removeIf(entry -> {
            OrganizationServiceSubscription subscription = entry.getValue();
            return subscription.tenantId().equals(tenantId)
                    && subscription.organizationId().equals(organizationId)
                    && subscription.serviceCode().equals(serviceCode);
        }));
    }
}
