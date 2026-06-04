package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LookupTableSpringDataRepository
        extends ReactiveCrudRepository<LookupTableEntity, UUID> {

    Mono<LookupTableEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<LookupTableEntity> findByTenantIdAndCode(UUID tenantId, String code);

    Flux<LookupTableEntity> findAllByTenantIdAndCountryCode(UUID tenantId, String countryCode);
}
