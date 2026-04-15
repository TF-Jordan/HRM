package yowyob.comops.api.inventory.adapter.out.persistence;

import yowyob.comops.api.inventory.application.port.out.InventorySessionRepository;
import yowyob.comops.api.inventory.domain.model.InventorySession;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class InventorySessionR2dbcRepositoryAdapter implements InventorySessionRepository {

    private final InventorySessionSpringDataRepository repository;

    public InventorySessionR2dbcRepositoryAdapter(InventorySessionSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<InventorySession> save(InventorySession inventorySession) {
        InventorySessionEntity entity = new InventorySessionEntity(inventorySession.id(), inventorySession.tenantId(),
                inventorySession.createdAt(), inventorySession.updatedAt(), inventorySession.organizationId(),
                inventorySession.agencyId(), inventorySession.productId(), inventorySession.referenceNumber(),
                inventorySession.countedQuantity(), inventorySession.status());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<InventorySession> findById(UUID tenantId, UUID inventorySessionId) {
        return repository.findByIdAndTenantId(inventorySessionId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<InventorySession> findByOrganization(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    private InventorySession toDomain(InventorySessionEntity entity) {
        return InventorySession.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.productId(), entity.referenceNumber(),
                entity.countedQuantity(), entity.status());
    }
}
