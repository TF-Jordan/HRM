package yowyob.comops.api.resource.application.port.in;

import yowyob.comops.api.resource.domain.model.MaterialResource;
import reactor.core.publisher.Mono;

public interface RegisterMaterialResourceUseCase {

    Mono<MaterialResource> register(RegisterMaterialResourceCommand command);
}
