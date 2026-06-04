package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayElementSpringDataRepository extends ReactiveCrudRepository<PayElementEntity, UUID> {

    Mono<PayElementEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<PayElementEntity> findByTenantIdAndCode(UUID tenantId, String code);

    Flux<PayElementEntity> findAllByTenantIdAndCountryCodeOrderByDisplayOrder(
            UUID tenantId, String countryCode);

    Flux<PayElementEntity> findAllByTenantIdAndCountryCodeAndActiveOrderByDisplayOrder(
            UUID tenantId, String countryCode, boolean active);
}
