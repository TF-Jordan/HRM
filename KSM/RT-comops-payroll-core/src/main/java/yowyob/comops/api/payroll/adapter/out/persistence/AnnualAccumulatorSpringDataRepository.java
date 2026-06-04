package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AnnualAccumulatorSpringDataRepository
        extends ReactiveCrudRepository<AnnualAccumulatorEntity, UUID> {

    Mono<AnnualAccumulatorEntity> findByTenantIdAndEmployeeIdAndYear(
            UUID tenantId, UUID employeeId, int year);
}
