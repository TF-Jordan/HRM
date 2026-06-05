package yowyob.comops.api.payroll.application.port.in;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.TaxBracketTable;

import java.util.UUID;

/** Administration of the configurable progressive tax scales (IRPP and the like). */
public interface ManageTaxBracketUseCase {

    Mono<TaxBracketTable> createTable(CreateTaxBracketTableCommand command);

    Mono<TaxBracketTable> deactivateTable(UUID tableId);

    Mono<TaxBracketTable> getTable(UUID tableId);

    Flux<TaxBracketTable> listTables(String countryCode);
}
