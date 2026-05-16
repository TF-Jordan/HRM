package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PerformanceReviewSpringDataRepository extends ReactiveCrudRepository<PerformanceReviewEntity, UUID> {

    Mono<PerformanceReviewEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<PerformanceReviewEntity> findAllByTenantIdAndEmployeeId(UUID tenantId, UUID employeeId);

    Flux<PerformanceReviewEntity> findAllByTenantIdAndOrganizationIdAndPeriode(UUID tenantId, UUID organizationId, String periode);
}
