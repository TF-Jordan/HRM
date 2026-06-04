package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FinalSettlementSpringDataRepository
        extends ReactiveCrudRepository<FinalSettlementEntity, UUID> {

    Mono<FinalSettlementEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<FinalSettlementEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<FinalSettlementEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
