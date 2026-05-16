package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.ProductTransformation;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListProductTransformationsUseCase {

    Flux<ProductTransformation> listTransformations(UUID organizationId, UUID agencyId);
}
