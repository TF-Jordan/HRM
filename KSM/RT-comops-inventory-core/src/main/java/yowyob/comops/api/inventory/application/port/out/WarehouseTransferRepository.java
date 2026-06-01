package yowyob.comops.api.inventory.application.port.out;

import yowyob.comops.api.inventory.domain.model.WarehouseTransfer;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WarehouseTransferRepository {
    Mono<WarehouseTransfer> save(WarehouseTransfer transfer);
    Mono<WarehouseTransfer> findById(UUID transferId);
    Flux<WarehouseTransfer> findByOrganization(UUID tenantId, UUID organizationId);
}
