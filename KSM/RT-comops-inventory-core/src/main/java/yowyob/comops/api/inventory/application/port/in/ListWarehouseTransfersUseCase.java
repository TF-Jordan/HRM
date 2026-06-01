package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.WarehouseTransfer;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListWarehouseTransfersUseCase {

    Flux<WarehouseTransfer> listTransfers(UUID organizationId);
}
