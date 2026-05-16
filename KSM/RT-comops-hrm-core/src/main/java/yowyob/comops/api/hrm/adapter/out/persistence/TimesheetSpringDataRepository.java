package yowyob.comops.api.hrm.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TimesheetSpringDataRepository extends ReactiveCrudRepository<TimesheetEntity, UUID> {

    Mono<TimesheetEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<TimesheetEntity> findAllByTenantIdAndEmployeeIdAndPeriode(UUID tenantId, UUID employeeId, String periode);

    Flux<TimesheetEntity> findAllByTenantIdAndOrganizationIdAndPeriode(UUID tenantId, UUID organizationId, String periode);

    Flux<TimesheetEntity> findAllByTenantIdAndOrganizationIdAndPeriodeAndStatus(
            UUID tenantId, UUID organizationId, String periode, String status);
}
