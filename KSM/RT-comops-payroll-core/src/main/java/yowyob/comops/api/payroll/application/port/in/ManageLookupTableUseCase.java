package yowyob.comops.api.payroll.application.port.in;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.LookupTable;

import java.util.UUID;

/** Administration of the configurable stepped forfait scales (RAV, TDL…). */
public interface ManageLookupTableUseCase {

    Mono<LookupTable> createTable(CreateLookupTableCommand command);

    Mono<LookupTable> deactivateTable(UUID tableId);

    Mono<LookupTable> activateTable(UUID tableId);

    Mono<LookupTable> getTable(UUID tableId);

    Flux<LookupTable> listTables(String countryCode);
}
