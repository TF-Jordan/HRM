package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaxBracketTableSpringDataRepository
        extends ReactiveCrudRepository<TaxBracketTableEntity, UUID> {

    Mono<TaxBracketTableEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<TaxBracketTableEntity> findByTenantIdAndCode(UUID tenantId, String code);

    Flux<TaxBracketTableEntity> findAllByTenantIdAndCountryCode(UUID tenantId, String countryCode);
}
