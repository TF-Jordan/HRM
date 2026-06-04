package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.TaxBracketTable;

import java.util.UUID;

/** Persistence port for {@link TaxBracketTable} (progressive scales + their brackets). */
public interface TaxBracketTableRepository {

    Mono<TaxBracketTable> save(TaxBracketTable table);

    Mono<TaxBracketTable> findById(UUID tenantId, UUID id);

    Mono<TaxBracketTable> findByCode(UUID tenantId, String code);

    Flux<TaxBracketTable> findByCountry(UUID tenantId, String countryCode);
}
