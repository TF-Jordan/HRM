package yowyob.comops.api.payroll.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayVariableSpringDataRepository extends ReactiveCrudRepository<PayVariableEntity, UUID> {

    Mono<PayVariableEntity> findByTenantIdAndEmployeeIdAndPeriode(
            UUID tenantId, UUID employeeId, String periode);

    Flux<PayVariableEntity> findAllByTenantIdAndOrganizationIdAndPeriode(
            UUID tenantId, UUID organizationId, String periode);
}
