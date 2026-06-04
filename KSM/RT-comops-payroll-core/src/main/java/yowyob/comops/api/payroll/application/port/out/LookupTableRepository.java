package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.LookupTable;

import java.util.UUID;

/** Persistence port for {@link LookupTable} (RAV/TDL stepped forfaits + their entries). */
public interface LookupTableRepository {

    Mono<LookupTable> save(LookupTable table);

    Mono<LookupTable> findById(UUID tenantId, UUID id);

    Mono<LookupTable> findByCode(UUID tenantId, String code);

    Flux<LookupTable> findByCountry(UUID tenantId, String countryCode);
}
