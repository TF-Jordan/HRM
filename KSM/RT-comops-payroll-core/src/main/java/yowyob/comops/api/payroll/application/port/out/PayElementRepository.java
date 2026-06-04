package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.PayElement;

import java.util.UUID;

/** Persistence port for {@link PayElement} (the configurable rubric catalogue). */
public interface PayElementRepository {

    Mono<PayElement> save(PayElement element);

    Mono<PayElement> findById(UUID tenantId, UUID id);

    Mono<PayElement> findByCode(UUID tenantId, String code);

    /** All elements defined for a country, regardless of active flag, for administration. */
    Flux<PayElement> findByCountry(UUID tenantId, String countryCode);

    /** Active elements for a country (the calculation catalogue). */
    Flux<PayElement> findActiveByCountry(UUID tenantId, String countryCode);
}
