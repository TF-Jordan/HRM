package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.WarehouseTransfer;
import reactor.core.publisher.Mono;

public interface CreateWarehouseTransferUseCase {
    Mono<WarehouseTransfer> createTransfer(CreateWarehouseTransferCommand command);
}
