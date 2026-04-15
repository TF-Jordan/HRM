package yowyob.comops.api.inventory.adapter.out.persistence;

import yowyob.comops.api.inventory.application.port.out.WarehouseTransferRepository;
import yowyob.comops.api.inventory.domain.model.WarehouseTransfer;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class WarehouseTransferR2dbcRepositoryAdapter implements WarehouseTransferRepository {

    private final WarehouseTransferSpringDataRepository repository;

    public WarehouseTransferR2dbcRepositoryAdapter(WarehouseTransferSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<WarehouseTransfer> save(WarehouseTransfer transfer) {
        WarehouseTransferEntity entity = new WarehouseTransferEntity(transfer.id(), transfer.tenantId(),
                transfer.createdAt(), transfer.updatedAt(), transfer.organizationId(), transfer.sourceAgencyId(),
                transfer.targetAgencyId(), transfer.productId(), transfer.referenceNumber(), transfer.quantity(),
                transfer.status());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<WarehouseTransfer> findById(UUID transferId) {
        return repository.findById(transferId)
                .map(this::toDomain);
    }

    @Override
    public Flux<WarehouseTransfer> findByOrganization(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    private WarehouseTransfer toDomain(WarehouseTransferEntity entity) {
        return WarehouseTransfer.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.sourceAgencyId(), entity.targetAgencyId(), entity.productId(),
                entity.referenceNumber(), entity.quantity(), entity.status());
    }
}
