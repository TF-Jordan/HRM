package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.ProductTransformation;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ValidateProductTransformationUseCase {

    Mono<ProductTransformation> validateTransformation(UUID transformationId);
}
