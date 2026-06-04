package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LookupEntrySpringDataRepository extends ReactiveCrudRepository<LookupEntryEntity, UUID> {

    Flux<LookupEntryEntity> findAllByTenantIdAndTableIdOrderByOrdre(UUID tenantId, UUID tableId);

    Mono<Void> deleteAllByTenantIdAndTableId(UUID tenantId, UUID tableId);
}
