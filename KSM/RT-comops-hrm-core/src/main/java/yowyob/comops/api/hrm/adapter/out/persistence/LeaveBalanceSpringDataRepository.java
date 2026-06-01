package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveBalanceSpringDataRepository extends ReactiveCrudRepository<LeaveBalanceEntity, UUID> {

    Mono<LeaveBalanceEntity> findByTenantIdAndEmployeeIdAndTypeAndAnnee(UUID tenantId, UUID employeeId, String type, int annee);

    Flux<LeaveBalanceEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<LeaveBalanceEntity> findAllByTenantIdAndEmployeeIdAndAnnee(UUID tenantId, UUID employeeId, int annee);
}
