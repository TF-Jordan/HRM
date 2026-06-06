package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseReportSpringDataRepository extends ReactiveCrudRepository<ExpenseReportEntity, UUID> {

    Mono<ExpenseReportEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<ExpenseReportEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<ExpenseReportEntity> findAllByTenantId(UUID tenantId);

    Flux<ExpenseReportEntity> findAllByTenantIdAndStatus(UUID tenantId, String status);

    Flux<ExpenseReportEntity> findAllByTenantIdAndMissionOrderId(UUID tenantId, UUID missionOrderId);
}
