package yowyob.comops.api.inventory.adapter.out.persistence;

import yowyob.comops.api.inventory.application.port.out.StockMovementRepository;
import yowyob.comops.api.inventory.domain.model.StockMovement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryStockMovementRepository implements StockMovementRepository {

    private final Map<UUID, StockMovement> stockMovements = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByReference(UUID tenantId, UUID organizationId, String referenceNumber) {
        return Mono.fromSupplier(() -> stockMovements.values().stream()
                .filter(stockMovement -> stockMovement.tenantId().equals(tenantId))
                .filter(stockMovement -> stockMovement.organizationId().equals(organizationId))
                .anyMatch(stockMovement -> stockMovement.referenceNumber().equalsIgnoreCase(referenceNumber)));
    }

    @Override
    public Mono<StockMovement> findById(UUID tenantId, UUID stockMovementId) {
        return Mono.justOrEmpty(stockMovements.get(stockMovementId))
                .filter(stockMovement -> stockMovement.tenantId().equals(tenantId));
    }

    @Override
    public Flux<StockMovement> findByAgencyAndProduct(UUID tenantId, UUID organizationId, UUID agencyId, UUID productId) {
        return Flux.fromStream(stockMovements.values().stream()
                .filter(stockMovement -> stockMovement.tenantId().equals(tenantId))
                .filter(stockMovement -> stockMovement.organizationId().equals(organizationId))
                .filter(stockMovement -> stockMovement.agencyId().equals(agencyId))
                .filter(stockMovement -> stockMovement.productId().equals(productId)));
    }

    @Override
    public Flux<StockMovement> findByAgency(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Flux.fromStream(stockMovements.values().stream()
                .filter(stockMovement -> stockMovement.tenantId().equals(tenantId))
                .filter(stockMovement -> stockMovement.organizationId().equals(organizationId))
                .filter(stockMovement -> stockMovement.agencyId().equals(agencyId)));
    }

    @Override
    public Flux<StockMovement> findByOrganization(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(stockMovements.values().stream()
                .filter(stockMovement -> stockMovement.tenantId().equals(tenantId))
                .filter(stockMovement -> stockMovement.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<StockMovement> save(StockMovement stockMovement) {
        return Mono.fromSupplier(() -> {
            stockMovements.put(stockMovement.id(), stockMovement);
            return stockMovement;
        });
    }
}
