package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ContractSpringDataRepository extends ReactiveCrudRepository<ContractEntity, UUID> {

    Mono<ContractEntity> findByTenantIdAndEmployeeIdAndStatus(UUID tenantId, UUID employeeId, String status);

    Flux<ContractEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);
}
