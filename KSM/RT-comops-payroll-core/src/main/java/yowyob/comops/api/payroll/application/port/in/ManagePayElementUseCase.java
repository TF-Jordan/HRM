package yowyob.comops.api.payroll.application.port.in;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.PayElement;

import java.util.UUID;

/** Administration of the configurable pay-element catalogue. */
public interface ManagePayElementUseCase {

    Mono<PayElement> createPayElement(CreatePayElementCommand command);

    Mono<PayElement> deactivatePayElement(UUID payElementId);

    Mono<PayElement> getPayElement(UUID payElementId);

    Flux<PayElement> listPayElements(String countryCode);
}
