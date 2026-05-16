package yowyob.comops.api.inventory.adapter.out.persistence;

import yowyob.comops.api.inventory.application.port.out.WarehouseTransferRepository;
import yowyob.comops.api.inventory.domain.model.WarehouseTransfer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryWarehouseTransferRepository implements WarehouseTransferRepository {
    private final Map<UUID, WarehouseTransfer> transfers = new ConcurrentHashMap<>();

    @Override
    public Mono<WarehouseTransfer> save(WarehouseTransfer transfer) {
        return Mono.fromSupplier(() -> {
            transfers.put(transfer.id(), transfer);
            return transfer;
        });
    }

    @Override
    public Mono<WarehouseTransfer> findById(UUID transferId) {
        return Mono.justOrEmpty(transfers.get(transferId));
    }

    @Override
    public Flux<WarehouseTransfer> findByOrganization(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(transfers.values().stream()
                .filter(transfer -> transfer.tenantId().equals(tenantId))
                .filter(transfer -> transfer.organizationId().equals(organizationId)));
    }
}
