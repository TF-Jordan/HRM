package yowyob.comops.api.inventory.application.port.in;

import yowyob.comops.api.inventory.domain.model.ProductTransformation;
import reactor.core.publisher.Mono;

public interface RecordTransformationUseCase {
    Mono<ProductTransformation> recordTransformation(RecordTransformationCommand command);
}
