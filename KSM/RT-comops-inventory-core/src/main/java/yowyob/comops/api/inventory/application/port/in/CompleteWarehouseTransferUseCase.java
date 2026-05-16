package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.WarehouseTransfer;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface CompleteWarehouseTransferUseCase {

    Mono<WarehouseTransfer> complete(UUID transferId);
}
