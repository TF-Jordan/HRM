package yowyob.comops.api.inventory.application.service;

import yowyob.comops.api.inventory.application.port.in.CompleteWarehouseTransferUseCase;
import yowyob.comops.api.inventory.application.port.out.WarehouseTransferRepository;
import yowyob.comops.api.inventory.domain.WarehouseTransferNotFoundException;
import yowyob.comops.api.inventory.domain.model.WarehouseTransfer;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.port.out.ReactiveTransactionalExecutor;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class WarehouseTransferCompletionService implements CompleteWarehouseTransferUseCase {

    private final WarehouseTransferRepository warehouseTransferRepository;
    private final BusinessEventPublisher businessEventPublisher;
    private final ReactiveTransactionalExecutor transactionalExecutor;

    public WarehouseTransferCompletionService(WarehouseTransferRepository warehouseTransferRepository,
            BusinessEventPublisher businessEventPublisher,
            ReactiveTransactionalExecutor transactionalExecutor) {
        this.warehouseTransferRepository = warehouseTransferRepository;
        this.businessEventPublisher = businessEventPublisher;
        this.transactionalExecutor = transactionalExecutor;
    }

    @Override
    public Mono<WarehouseTransfer> complete(UUID transferId) {
        return transactionalExecutor.transactional(warehouseTransferRepository.findById(transferId)
                .switchIfEmpty(Mono.error(new WarehouseTransferNotFoundException(transferId)))
                .map(WarehouseTransfer::complete)
                .flatMap(warehouseTransferRepository::save)
                .flatMap(saved -> businessEventPublisher.publish(transferCompletedEvent(saved)).thenReturn(saved)));
    }

    private BusinessEvent transferCompletedEvent(WarehouseTransfer transfer) {
        return BusinessEvent.now(transfer.tenantId(), transfer.organizationId(), "WAREHOUSE_TRANSFER_COMPLETED",
                "WAREHOUSE_TRANSFER", transfer.id(), payload(
                        "referenceNumber", transfer.referenceNumber(),
                        "sourceAgencyId", transfer.sourceAgencyId(),
                        "targetAgencyId", transfer.targetAgencyId(),
                        "productId", transfer.productId(),
                        "quantity", transfer.quantity(),
                        "status", transfer.status()));
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            payload.put(entries[index].toString(), entries[index + 1]);
        }
        return payload;
    }
}
