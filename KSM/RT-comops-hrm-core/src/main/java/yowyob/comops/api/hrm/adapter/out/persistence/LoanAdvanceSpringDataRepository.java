package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanAdvanceSpringDataRepository extends ReactiveCrudRepository<LoanAdvanceEntity, UUID> {

    Mono<LoanAdvanceEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<LoanAdvanceEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<LoanAdvanceEntity> findAllByTenantIdAndEmployeeIdAndStatus(UUID tenantId, UUID employeeId, String status);
}
