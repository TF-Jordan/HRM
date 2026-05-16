package yowyob.comops.api.inventory.adapter.out.persistence;

import yowyob.comops.api.inventory.application.port.out.StockMovementRepository;
import yowyob.comops.api.inventory.domain.model.StockMovement;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class StockMovementR2dbcRepositoryAdapter implements StockMovementRepository {

    private final StockMovementSpringDataRepository repository;

    public StockMovementR2dbcRepositoryAdapter(StockMovementSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByReference(UUID tenantId, UUID organizationId, String referenceNumber) {
        return repository.existsByTenantIdAndOrganizationIdAndReferenceNumberIgnoreCase(tenantId, organizationId,
                referenceNumber);
    }

    @Override
    public Mono<StockMovement> findById(UUID tenantId, UUID stockMovementId) {
        return repository.findByIdAndTenantId(stockMovementId, tenantId)
                .map(this::toDomain);
    }

    @Override
    public Flux<StockMovement> findByAgencyAndProduct(UUID tenantId, UUID organizationId, UUID agencyId,
            UUID productId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyIdAndProductId(tenantId, organizationId, agencyId,
                        productId)
                .map(this::toDomain);
    }

    @Override
    public Flux<StockMovement> findByAgency(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    @Override
    public Flux<StockMovement> findByOrganization(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Mono<StockMovement> save(StockMovement stockMovement) {
        StockMovementEntity entity = new StockMovementEntity(stockMovement.id(), stockMovement.tenantId(),
                stockMovement.createdAt(), stockMovement.updatedAt(), stockMovement.organizationId(),
                stockMovement.agencyId(), stockMovement.productId(), stockMovement.thirdPartyId(),
                stockMovement.referenceNumber(), stockMovement.sourceDocumentType(),
                stockMovement.sourceDocumentNumber(), stockMovement.movementType(), stockMovement.quantity(),
                stockMovement.status());
        return repository.save(entity).map(this::toDomain);
    }

    private StockMovement toDomain(StockMovementEntity entity) {
        return StockMovement.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.productId(), entity.thirdPartyId(),
                entity.referenceNumber(), entity.sourceDocumentType(), entity.sourceDocumentNumber(),
                entity.movementType(), entity.quantity(), entity.status());
    }
}
